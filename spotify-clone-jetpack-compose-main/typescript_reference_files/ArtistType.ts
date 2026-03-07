import { HomePageDataType } from "./HomePageData.type"
import { PlayListType } from "./PlayListType"
import { SongType } from "./Song.type"

export type ArtistType={
    image:string,
    image_type:string,
    id:string,
    title:string,
    monthly_listeners:number,
    popular_songs:SongType[],
    featured_albums:HomePageDataType,
    popular_releases:HomePageDataType,
    singles:HomePageDataType,
    albums_featuring_artist:HomePageDataType,
    fans_also_like:HomePageDataType,
    appears_on:HomePageDataType,
    dicovered_on:HomePageDataType,
    genres:string[],
    similar:ArtistListingType[]

}

export type ArtistListingType={
    image:string,
    id:string,
    title:string,
}


export type AllPlayListType={
    image:string,
    id:string,
    title:string,
}