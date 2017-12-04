(ns website.core
  (:require [clojure.string :as string]
            [hiccup.page :refer [html5]]
            [clj-time.coerce :as tc]
            [clj-time.format :as tf]))

(def site-name "adamrenklint.com")
(def tag-line "Hey, I'm Adam")
(def archive-url "/archive.html")

(defn group-by-year [entries]
  (reduce
    (fn [years entry]
      (let [year (tf/unparse (tf/formatter "y")
                             (tc/from-date (:date entry)))]
        (update years year
          #(if %
             (conj % entry)
             [entry]))))
    {}
    entries))

(defn render-post-link [{:keys [title date permalink]}]
  [:li
   [:a {:href permalink}
    title]])

(defn header [permalink]
  (let [header [:div.header
                [:img.face {:src "/images/avatar.jpg"}]
                [:div.tag-line tag-line]]
        index? (= "/" permalink)]
    (if index?
      header
      [:a.home {:href "/"} header])))

(defn footer []
  [:div.footer
   "Want to get in touch? "
   [:a {:href "mailto:adam@renklint.com"} "Send a mail"]
   ", "
   [:a {:href "https://twitter.com/adamrenklint"} "tweet at me"]
   " or "
   [:a {:href "https://github.com/adamrenklint"} "open a pull request"]
   "."])

(defn- format-date [date]
  (tf/unparse (tf/formatter "MMMM d, y") (tc/from-date date)))

(defn page [title permalink & content]
  (html5
   [:head
    [:title (str (or title tag-line) " - " site-name)]
    [:meta {:http-equiv "content-type"
            :content "text/html;charset=UTF-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1.0,
    minimum-scale=1.0, maximum-scale=1.0, user-scalable=no"}]
    [:link {:rel "stylesheet"
            :href "//fonts.googleapis.com/css?family=Source+Serif+Pro:400,700"}]
    [:link {:rel "stylesheet" :href "/styles/core.css"}]
    [:link {:rel "stylesheet" :href "/styles/pygments.css"}]]
   [:body
    (header permalink)
    [:div.content
     (when title [:h1 title])
     content
     (footer)]
    [:script {:src "/scripts/main.js" :type "text/javascript"}]]))

(defn index [{:keys [entry entries]}]
  (let [{:keys [permalink]} entry]
    (page nil permalink
      [:h1 "I make music and things with code"]
      [:p "An experienced full stack web developer from Sweden, based in Berlin. Functional programmer in training, passionate about Clojure/Script."]
      [:p "Web Audio nerd and wonky beatmaker, digging in dusty crates and chopping samples. Student of algorithmic composition and live coding. Hacking on ideas to aid and improve creative expression."]
      [:p "Previously at Microsoft and Wunderlist."]
      [:h2 "Projects"]
      [:ul
       [:li [:a {:href "https://trn.gl"} "trn.gl"] " is a music programming language and development environment"]
       [:li [:a {:href "https://github.com/adamrenklint/prost"} "prost"] " asserts arguments and function return values in ClojureScript"]
       [:li [:a {:href "https://github.com/adamrenklint/strukt"} "strukt"] " helps to create ClojureScript maps and validate their shape"]
       [:li [:a {:href "http://bapjs.org"} "Bap"] " is a library for writing music with Javascript in modern browsers"]]
      [:h2 "Posts"]
      [:ul
       (map render-post-link (take 3 entries))]
      [:p
       "For more thoughts, experiments and ideas: "
       [:a {:href archive-url} "browse the archive"]
       "."])))

(defn post [{{:keys [permalink date title content script]} :entry}]
  (page title permalink
     content
     [:div.post-footer
      [:div
       "Published on " (format-date date) "."]
      [:div
       [:a {:href archive-url} "Browse the archive"]
       " or "
       [:a {:href "/"} "go to the homepage"]
       "."]]
     (when script
       [:script {:src (str "/scripts/" script)
                 :type "text/javascript"}])))

(defn archive [{:keys [entry entries]}]
  (let [{:keys [permalink]} entry]
    (page "Archive" permalink
      (map
        (fn [[year entries]]
          [:div
            [:h2 year]
            [:ul (map render-post-link entries)]])
        (group-by-year entries)))))

(defn not-found [{{:keys [permalink]} :entry}]
  (page "Page not found" permalink
    "Sorry, there is no page at this url. But hey, there's lots of other stuff to read and explore here. "
    [:a {:href "/"} "Go back to the homepage"]
    " or "
    [:a {:href archive-url} "browse the archive"]
    "."))
