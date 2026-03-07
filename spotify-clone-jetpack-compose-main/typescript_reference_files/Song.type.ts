import { PodcastDto } from "./Podcast.dto"
import { RadioStation } from "./RadioStationType"

export type SongType = {
    id: string,
    name: string,
    playlist_id: string,
    playlist_name: string,
    artists: { title: string, id: string, path: string }[],
    duration: number,//seconds
    likes: number,
    genres: string[],
    album: { title: string, id: string, path: string },
    thumbnail: string,
    view_count: number,
    preview_url: string,
    s3link: string
}



export enum SearchTypeEnum { album = 'album', songs = 'songs', artist = 'artist', radio = 'radio', podcast = 'podcast' }


export type SearchPageResType = {
    total: number,
    took: number,
    cards: { play_url:string,id:string,image: string, name: string, artist: string, type: SearchTypeEnum }[],
    type: SearchTypeEnum
}