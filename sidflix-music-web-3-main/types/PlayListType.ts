import { SongType } from "./Song.type"

export type PlayListType={
    id:number,
    image:string,
    title:string,
    artists:{id:string,name:string}[],
    songs:SongType[],

}