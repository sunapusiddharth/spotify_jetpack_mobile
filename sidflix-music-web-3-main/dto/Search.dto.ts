export type SearchType = {

    "took": number,
    "timed_out": boolean,
    "_shards": {
        "total": number,
        "successful": number,
        "skipped": number,
        "failed": number
    },
    "hits": {
        "total": {
            "value": number,
            "relation": string
        },
        "max_score": number,
        "hits": SearchHitType[]
    }

}

export type SearchHitType = {

    "_index": string,
    "_type": string,
    "_id": string,
    "_score": number,
    "_source": {
        id: string;
        title: string;
        album: string;
        artists: string
        type: string;
        release_date: string;
        poster_path: string;
        popularity: number;
        score: number;
        genres: string;
        available: boolean;
        preview_url:string;
    },
    "highlight": {
        "title": string[]
    }
}


export type SearchAggType = {
    "took": number,
    "timed_out": boolean,
    "_shards": {
        "total": number,
        "successful": number,
        "skipped": number,
        "failed": number
    },
    "hits": {
        "total": {
            "value": number,
            "relation": string
        },
        "max_score": number,
        "hits": SearchHitType[]
    },
    "aggregations": {
        "states": {
            "doc_count": 2962,
            "tags_here": {
                "doc_count_error_upper_bound": 0,
                "sum_other_doc_count": 3748,
                "buckets": {
                    "key": string,
                    "doc_count": number
                }[]
            }
        }
    }
}

export type SimpleSearchAggType = {
    "took": number,
    "timed_out": boolean,
    "_shards": {
        "total": number,
        "successful": number,
        "skipped": number,
        "failed": number
    },
    "hits": {
        "total": {
            "value": number,
            "relation": string
        },
        "max_score": number,
        "hits": SearchHitType[]
    },
    "aggregations": {
            "tags_here": {
                "doc_count_error_upper_bound": 0,
                "sum_other_doc_count": 3748,
                "buckets": {
                    "key": string,
                    "doc_count": number
                }[]
            }
    }
}

export interface MeiliSearchAggType {
    value:string,count:number
}