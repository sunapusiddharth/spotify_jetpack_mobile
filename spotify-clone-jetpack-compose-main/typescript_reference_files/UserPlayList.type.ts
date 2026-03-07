import { SongType } from "./Song.type"

export type UserPlayListType = {
    id: string,
    name: string,

    image: string,
    tracks: string[
    ],
    user_id: string
    followers: string[]
    created_at: string
    updated_at: string
}