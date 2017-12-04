(set-env!
  :source-paths #{"src" "content" "styles"}
  :resource-paths #{"resources"}
  :dependencies '[[perun                "0.4.2-SNAPSHOT"]
                  [hiccup               "1.0.5"]
                  [adzerk/boot-cljs     "2.1.4"]
                  [deraen/boot-sass     "0.2.1"]
                  [clj-time             "0.14.2"]
                  [pandeiro/boot-http   "0.8.3"  :scope "test"]
                  [adzerk/boot-reload   "0.4.13" :scope "test"]])

(ns-unmap 'boot.user 'trace)

(require '[website.core       :as website]
         '[clojure.string     :as string]
         '[io.perun           :refer :all]
         '[deraen.boot-sass   :refer [sass]]
         '[adzerk.boot-cljs   :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]])

(defn- post? [{:keys [type]}]
  (= "post" type))

(defn- post-collection [renderer page]
  (collection :renderer renderer
              :page     page
              :filterer post?
              :sortby   #(:date %)))

(deftask build
  [p prod? bool "Build for production use?"]
  (comp (sass)
        (cljs :optimizations (if prod? :advanced :none))
        (sift :move {#"core.css" "public/styles/core.css"})
        (markdown :md-exts {:anchorlinks true})
        (highlight)
        (post-collection 'website.core/index "index.html")
        (post-collection 'website.core/archive "archive.html")
        (render :renderer 'website.core/post  :filterer post?)
        (static :renderer 'website.core/not-found :page "404.html")))

(deftask dev []
  (comp (watch)
        (reload :asset-path "public")
        (build)
        (serve :resource-root "public")))

(deftask prod []
  (comp (build :prod? true)
        (target)))

(task-options!
  pom {:project 'adamrenklint.website
       :version "8.0.0"})
