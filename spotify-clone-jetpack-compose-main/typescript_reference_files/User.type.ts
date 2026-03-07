import { UserPlayListType } from "./UserPlayList.type"

export type UserType = {
    id:string,
    name:string,
    playlists:UserPlayListType[],
    liked_songs:string[],
    liked_podcast:string[],
    liked_radio:string[],
    tracks:string[],
    artists:string[]
}