(ns muzak.core.hdf5-parse)

;;******************************David**********************************


; create a java.io.file from the MillionSongSubset folder, return lazy seq of all files
(defn get-h5-files []
  (def f (clojure.java.io/file "resources/MillionSongSubset/data"))
  (defn is-not-dir [f] (not (.isDirectory f))) ; simple predicate

  ;(seq (.listFiles f)) ;Java.io.file has a "listFiles" method
  (filter is-not-dir (file-seq f)))

; wasn't sure how to pass the ".getPath" to map
(defn get-path [f] (.getPath f))

; Java Interop -call HDF5Factory to retrieve a HDF5Reader instance
(defn get-h5-reader [path]
  (. ch.systemsx.cisd.hdf5.HDF5Factory openForReading path))

; NOTE: I believe that the Song_Title is part of a compound data set, needing a compound reader
(defn hdf5-getCompoundHdf5Reader [hr]
  (.compound hr))

;gets an array of terms (data) from an hdf5 reader
(defn get-terms [hr]
  (def sr (.string hr)) ;Get a HDF5StringReader
  (vec (.readArray sr "/metadata/artist_terms")))

; color generator
(def dot-colors ["blue" "green" "yellow" "purple" "red" "cyan"])

(let [number-colors (count dot-colors)]
  (defn rand-color []
    (get dot-colors (rand-int number-colors))))

;Get the "title" field from the Compound DataSet "/metadata/songs" for given relative path
(defn get-song [path]
  (def hr (get-h5-reader path))
  (def cr (hdf5-getCompoundHdf5Reader hr))
  (def rec (.read cr "/metadata/songs" ch.systemsx.cisd.hdf5.HDF5CompoundDataMap))
  (let [bubble_size 100] ; default mulitplier

    {:name (get rec "title"), :artist (get rec "artist_name"), :album (get rec "release"),
       :id (get rec "song_id")

       :terms (get-terms hr),
       :fill (rand-color),

       :size (* bubble_size (get rec "song_hotttnesss")) ;default bubble size field (client can choose other field)

       :artist_hottness (* bubble_size (get rec "artist_hotttnesss"))
       :song_hottness (* bubble_size (get rec "song_hotttnesss"))
       :artist_familiarity (* bubble_size (get rec "artist_familiarity"))
       :artist_latitude (get rec "artist_latitude")
       :artist_longitude (get rec "artist_longitude")
     }))

;returns true if song:terms contains term, otherwise returns false
(defn check-song [term, song]
  (some? (some #{term} (get song :terms))))

;returns a vector subset of songs where :terms contains filter-term
(defn filter-songs [filter-term, songs]
  (filter (partial check-song filter-term) songs))

(defn get-filtered-songs [f, p]
  (filter-songs f (map get-song p)))

; ex: (write-edn "resources/public/muzak.edn")
(defn write-edn [obj f-out]
  (spit f-out (pr-str obj)))

;Magic (since it doesn't take any client search params) - overwrites resources/public/muzak.edn
(defn magic-build-edn [client-filter-term]

  ;; WARNING - On some machines, attempting to open too many .h5 files (> 500) causes
  ;; NullPointerException H5Dread_string:  buf length <=0  ch.systemsx.cisd.hdf5.hdf5lib.H5.H5Dread_string (H5.java:-2)
  ;; WORKAROUND is to limit number of .h5 files that we consider

  ;(def all-paths (map get-path (get-h5-files)))

  (def all-paths (map get-path (take 500 (get-h5-files))))

  ;builds a lazy sequence of all paths in 10 item blocks
  ;(def paths (partition 10 10 nil all-paths))
  ;NOTE: JHDF5 API says we are supposed to close the readers - we are not (MEM-LEAK ??)

  ;Get first 100 song object if serach term is blank, else filter first 100
  (def children
    (cond
      (= client-filter-term "") (vec (take 100 (map get-song all-paths)))
       ;:else (take 10 (map (partial get-filtered-songs client-filter-term) paths))
       :else (vec (take 100 (get-filtered-songs client-filter-term all-paths)))
     ))
  {:name "all" :r 100 :fill "#3182bd" ;some defaults for parent bubble/circle
     :children children})

;Magic since it doesn't take client search request OR output destination to overwrite
(defn magic-write-edn [client-filter-term]
  (write-edn (magic-build-edn) "resources/public/muzak.edn"))


;Search 100 songs and return those that match client-filter-term
;ex `(magic-write-edn "rock")`
(defn magic-write-edn [client-filter-term]
  (write-edn (magic-build-edn client-filter-term) "resources/public/muzak.edn"))



;;******************************Josh**********************************

(def songs (atom ()))

(defn list-songs []
  ;;(response (json/encode @songs))
  )

(defn create-song [song-name]
  (swap! songs conj song-name)
  ;;(response "") 201
  )

;gets an hdf5 reader
(defn get-reader [f]
  (. ch.systemsx.cisd.hdf5.HDF5Factory openForReading f))

;gets a string reader from an hdf5 reader
(defn getStringHdf5Reader [hr]
  (.string hr))

;gets a compound reader from an hdf5 reader
(defn getCompoundHdf5Reader [hr]
  (.compound hr))

;gets an array of terms (data) from an hdf5 reader
(defn get-terms [hr]
  (def sr (getStringHdf5Reader hr))
  (vec (.readArray sr "/metadata/artist_terms")))

;test with testF
;gets a map of attribute-value pairs (tag-data pairs) from an h5 file
(comment
(defn get-song [f]
  (def hr (get-reader f))
  (def cr (getCompoundHdf5Reader hr))
  (def rec (.read cr "/metadata/songs" ch.systemsx.cisd.hdf5.HDF5CompoundDataMap))
  {:artist (get rec "artist_name"),
   :title (get rec "title"),
   :r (* 100 (get rec "song_hotttnesss"))
   :terms (get-terms hr)}))

;pass testF to get-song for testing
;;(def testF "resources/TRAXLZU12903D05F94.h5")

;Sorry Josh -- just trying to get order in file right, these moved above
;returns true if song:terms contains term, otherwise returns false
;(defn check-song [term, song]
;  (some? (some #{term} (get song :terms))))

;returns a list of maps that is a subset of songs where :terms contains term
;(defn filter-songs [term, songs]
;  (filter (partial check-song term) songs))

;tests for filter-songs. "pop" returns 1 result, "metal" returns 0 results
;(def s (get-song testF))
;(filter-songs "pop" [s])
;(filter-songs "metal" [s])



; HDF5 Parsing Experiments
;(defn hdf5-do-something []
;  (def wconfig (. ch.systemsx.cisd.hdf5.HDF5Factory configure "attribute.h5"))
;  (def writer (.writer wconfig))
;  (.close writer))

;builds a map of attribute-value pairs
;(defn get-song-data [rec, x]
;  {x (get rec x)})

;David_Edit my compiler is yelling at arguments "[hr] [x]" - is this legal or typo?
;(defn hdf5-get-compound [hr, x]
;  {(:attr x) (.getMemberInfo hr (:attr x))})

;(defn hdf5-getStringHdf5Reader [hr]
;  (.string hr))

;(defn hdf5-readStringArray [hr]
;  (def sr (hdf5-getStringHdf5Reader hr))
;  (.readArray sr "/metadata/similar_artists"))

; Read a CompoundDataSet into a Map and return the "title" value
;(defn hdf5-getTitle [cr]
;  (def rec (.read cr "/metadata/songs" ch.systemsx.cisd.hdf5.HDF5CompoundDataMap))
;  (get rec "title"))

; Silly function to delete during last iteration - reads song title with no params
;(defn hdf5-magic []
;  (def hr (hdf5-get-reader))
;  (def cr (hdf5-getCompoundHdf5Reader hr))
;  (hdf5-getTitle cr))

;(defn hdf5-get-attr [x]
;  (def hr (hdf5-get-reader))
;  (.getStringAttribute hr (get x :path) (get x :attr)))

;attempt at reading an arbitrary list of tags
;(def x [{:path "/metadata/atrist_terms" :attr "TITLE"}])
;(defn get-song-details [x]
;  (def hr (hdf5-get-reader))
;  (mapcat (partial hdf5-get-compound hr) x))
