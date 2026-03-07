import { SongType } from "./Song.type"

export type CardContentType={
    image:string,
    id:string,
    title:string,
    subtitle:string
    type:'playlist_card'|'music_card'|'artists_card',
    path:string,
    song:SongType | null
}