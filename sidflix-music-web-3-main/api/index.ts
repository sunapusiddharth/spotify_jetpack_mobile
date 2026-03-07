import axios from "axios";
import { PodcastsPageInfo } from "../components/podcast/PodcastCard";
import { MeiliSearchAggType, SearchAggType, SearchType } from "../dto/Search.dto";
import { AllPlayListType, ArtistListingType, ArtistType } from "../types/ArtistType";
import { HomePageDataType } from "../types/HomePageData.type";
import { PlayListType } from "../types/PlayListType";
import { PodcastCardDto } from "../types/PodcastCard.dto";
import { PodcastEpisodeDto } from "../types/PodcastEpisode.dto";
import { RadioStation, RadioStationType } from "../types/RadioStationType";
import { SearchPageResType, SearchTypeEnum, SongType } from "../types/Song.type";
import { UserType } from "../types/User.type";
import { UserPlayListType } from "../types/UserPlayList.type";

// axios.defaults.headers.get['Content-Type'] ='application/json';
// const content_base_url = 'http://[2602:ff16:13:0:1:55:0:1]/api/spotify'
const content_base_url = 'http://35.184.85.182/api/spotify'
const addition_base_url = 'http://localhost:5000'
const search_base_url = content_base_url
export const fetchUserData = async (id: string): Promise<UserType> => {
  const res = await axios.get<UserType>(`${content_base_url}/users/${id}`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};


export type HomePageInfo = {
  results: HomePageDataType[],
  page: number
}
export const fetchHomePageData = async (id: string, page: number): Promise<HomePageInfo> => {
  if (!id) return {
    page: page,
    results: []
  }

  const res = await axios.get<HomePageInfo>(`${content_base_url}/users/${id}/home/${page}`);

  if (res.status === 200) {
    console.log("homepagedata", res)
    return {
      page: res.data.page,
      results: res.data.results
    };
  }
  throw new Error('Network response not ok');
};


export const fetchPlaylistData = async (id: string): Promise<PlayListType | undefined> => {
  if (!id) return undefined
  const res = await axios.get<PlayListType>(`${content_base_url}/playlist/${id}`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const fetchAlbum = async (id: string): Promise<PlayListType> => {
  const res = await axios.get<PlayListType>(`${content_base_url}/album/${id}`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const freshAlbums = async (): Promise<PlayListType[]> => {
  const res = await axios.get<PlayListType[]>(`${content_base_url}/album/freshAlbums`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
}

export const fetchFeaturedHomePageData = async (id: string): Promise<PlayListType> => {
  const res = await axios.get<PlayListType>(`${content_base_url}/album/${id}`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};


export const fetchTopPlaylistCollectionData = async (): Promise<PlayListType[]> => {
  const res = await axios.get<PlayListType[]>(`${content_base_url}/playlist_collection/top/10/0`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const fetchPlaylistCollection = async (id: string): Promise<PlayListType | null> => {
  if (!id) return null
  const res = await axios.get<PlayListType>(`${content_base_url}/playlist_collection/${id}`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const getuserLikedPlcs = async (userid: string) => {
  const res = await axios.get<HomePageDataType>(`${content_base_url}/playlist_collection/${userid}/liked_plc`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const getTopFollowingPlayListCollections = async () => {
  const res = await axios.get<HomePageDataType>(`${content_base_url}/playlist_collection/getTopFollowingPlayListCollections`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const getEditorsPlayList = async (limit: number) => {
  const res = await axios.get<HomePageDataType>(`${content_base_url}/playlist_collection/getEditorsPlayList/${limit}`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const getLatestPlayListCollections = async () => {
  const res = await axios.get<HomePageDataType>(`${content_base_url}/playlist_collection/getLatestPlayListCollections`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const getLastPlc = async (userid: string) => {
  const res = await axios.get<HomePageDataType>(`${content_base_url}/playlist_collection/${userid}/listened_plc/last_played`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};


export const userLikedPlc = async (userid: string, stationid: string) => {
  const res = await axios.put(`${content_base_url}/playlist_collection/${userid}/liked_plc/${stationid}`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const userListenedPlc = async (userid: string, stationid: string, duration: number) => {
  const res = await axios.put(`${content_base_url}/playlist_collection/${userid}/listened_plc/${stationid}/${duration}`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const fetchPlcGenres = async (userid: string) => {
  const res = await axios.get<SearchAggType>(`${search_base_url}/search/${userid}/plc_genres`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

//End playlist_collection


export const fetchQueueData = async (userid: string): Promise<SongType[]> => {
  const res = await axios.get(`${content_base_url}/tracks/${userid}/queue`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const getTopScoringSongsForuser = async (userid: string, limit: number): Promise<SongType[] | null> => {
  if (!userid) return null
  const res = await axios.get(`${content_base_url}/tracks/top_scoring_songs/${userid}/${limit}`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const getTopScoringSongs = async (limit: number): Promise<SongType[]> => {
  const res = await axios.get(`${content_base_url}/tracks/top_scoring_songs/${limit}`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};
export const getNextQueueItem = async (userid: string): Promise<{ song: SongType, updated_queue: SongType[] }> => {
  console.log("getNextQueueItem", userid)
  const res = await axios.get<{ song: SongType, updated_queue: SongType[] }>(`${content_base_url}/tracks/${userid}/next_item_in_queue`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};
export const getPrevQueueItem = async (userid: string): Promise<{ song: SongType, updated_queue: SongType[] }> => {
  const res = await axios.get<{ song: SongType, updated_queue: SongType[] }>(`${content_base_url}/tracks/${userid}/prev_item_in_queue`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};


export type ArtistSongsPagination = {
  results: SongType[],
  page: number
}
export const fetchArtistsSongs = async (id: string, page: number): Promise<ArtistSongsPagination> => {
  if (!id) return {
    page: page,
    results: []
  }

  const res = await axios.get<ArtistSongsPagination>(`${content_base_url}/artist/songs/${id}/${page}`);
  if (res.status === 200) {
    return {
      page: res.data.page,
      results: res.data.results
    };
  }
  throw new Error('Network response not ok');
};

export const fetchTopArtists = async (): Promise<ArtistType[] | null> => {
  const res = await axios.get<ArtistType[]>(`${content_base_url}/artist/top`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

// export const fetchArtistTopTracks = async (id: string): Promise<SongType[] | null> => {
//   const res = await axios.get<SongType[]>(`${content_base_url}/artist/${id}/top_tracks`);
//   if (res.status === 200) {
//     return res.data;
//   }
//   throw new Error('Network response not ok');
// };


// export const fetchArtistSingles = async (id: string): Promise<SongType[] | null> => {
//   const res = await axios.get<SongType[]>(`${content_base_url}/artist/${id}/singles`);
//   if (res.status === 200) {
//     return res.data;
//   }
//   throw new Error('Network response not ok');
// };

// export const fetchSimilarArtists = async (id: string, limit: number): Promise<ArtistType[] | null> => {
//   const res = await axios.get<ArtistType[]>(`${content_base_url}/artist/${id}/similar`);
//   if (res.status === 200) {
//     return res.data;
//   }
//   throw new Error('Network response not ok');
// };

export const fetchAllArtistPlaylists = async (id: string, limit: number, skip: number): Promise<PlayListType[] | null> => {
  const res = await axios.get<PlayListType[]>(`${content_base_url}/artist/${id}/playlists/${limit}/${skip}`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const fetchAllArtistPlaylistsCollection = async (id: string, limit: number, skip: number): Promise<PlayListType[] | null> => {
  const res = await axios.get<PlayListType[]>(`${content_base_url}/artist/${id}/playlists_collection/${limit}/${skip}`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};


export const fetchArtistData = async (id: string): Promise<ArtistType | null> => {
  console.log("Aritst quert", id)
  if (!id) return null
  const res = await axios.get<ArtistType>(`${content_base_url}/artist/${id}`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export type ArtistPaginationType = {
  results: ArtistListingType[],
  page: number
}
export const fetchAllArtistData = async (page: number): Promise<ArtistPaginationType> => {
  const res = await axios.get<ArtistPaginationType>(`${content_base_url}/artist/all/${page}`);
  if (res.status === 200) {
    return {
      page: res.data.page,
      results: res.data.results
    };
  }
  throw new Error('Network response not ok');
};
export type PlayListPaginationType = {
  results: ArtistListingType[],
  page: number
}
export const fetchAllPlayListData = async (page: string): Promise<PlayListPaginationType> => {
  const res = await axios.get<PlayListPaginationType>(`${content_base_url}/album/all/${page}`);
  if (res.status === 200) {
    return {
      page: res.data.page,
      results: res.data.results
    };
  }
  throw new Error('Network response not ok');
};
type PlayListCollectionPaginationType = {
  results: AllPlayListType[],
  page: number
}

export const fetchAllPlayListCollections = async (page: string): Promise<PlayListCollectionPaginationType> => {
  const res = await axios.get<PlayListCollectionPaginationType>(`${content_base_url}/playlist_collection/all/${page}`);
  if (res.status === 200) {
    return {
      page: res.data.page,
      results: res.data.results
    };
  }
  throw new Error('Network response not ok');
};


export const browsePlayListCollectionByGenre = async (genre: string, page: string): Promise<PlayListCollectionPaginationType> => {
  const res = await axios.get<PlayListCollectionPaginationType>(`${content_base_url}/playlist_collection/browseByGenre/${page}?genre=${genre}`);
  if (res.status === 200) {
    return {
      page: res.data.page,
      results: res.data.results
    };
  }
  throw new Error('Network response not ok');
};

export const browseTop10PlaylistCollectionByGenre = async (userid: string, page: string) => {
  const res = await axios.get<HomePageInfo>(`${content_base_url}/playlist_collection/browseTop10PlaylistCollectionByGenre/${userid}/${page}`);
  if (res.status === 200) {
    return res.data
  }
  throw new Error('Network response not ok');
};



function randomIntFromInterval(min: number, max: number) { // min and max included 
  return Math.floor(Math.random() * (max - min + 1) + min)
}

const randomElement = (data: string[]) => data[Math.floor(Math.random() * data.length)];

export const addToQueue = async (id: string, trackid: string) => {
  const res = await axios.put(`${content_base_url}/tracks/${id}/${trackid}`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const addPlayListToQueue = async (id: string, trackids: string[]) => {
  const res = await axios.post(`${content_base_url}/tracks/${id}/add-multiple`, {
    songs: trackids
  });
  if (res.status === 201) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const removeFromQueue = async (id: string, trackid: string) => {
  const res = await axios.delete(`${content_base_url}/tracks/${id}/${trackid}`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const followPlayList = async (id: string, playlistid: string) => {
  const res = await axios.put(`${content_base_url}/users/${id}/${playlistid}/follow-playlist`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const unFollowPlayList = async (id: string, playlistid: string) => {
  const res = await axios.put(`${content_base_url}/users/${id}/${playlistid}/unfollow-playlist`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const getSong = async (id: string) => {
  const res = await axios.get<SongType>(`${content_base_url}/tracks/${id}`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

// export const updateSong = async (song: SongType) => {
//   const res = await axios.put<SongType>(`${content_base_url}/tracks/${song.id}`, {
//     song
//   });
//   if (res.status === 201) {
//     return res.data;
//   }
//   throw new Error('Network response not ok');
// };




export const deleteSong = async (id: string) => {
  const res = await axios.delete<string>(`${content_base_url}/tracks/${id}`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};



export const liekdislikesong = async (id: string, likedislike: boolean, trackid: string) => {
  const res = await axios.put(`${content_base_url}/users/${id}/likedislike/song/${trackid}/${likedislike}`);
  return res.data
  throw new Error('Network response not ok');
};

export const liekdislikestation = async (id: string, likedislike: boolean, trackid: string) => {
  const res = await axios.put(`${content_base_url}/users/${id}/likedislike/radio/${trackid}/${likedislike}`);
  return res.data
  throw new Error('Network response not ok');
};

export const liekdislikepodcast = async (id: string, likedislike: boolean, trackid: string) => {
  const res = await axios.put(`${content_base_url}/users/${id}/likedislike/podcast/${trackid}/${likedislike}`);
  return res.data
  throw new Error('Network response not ok');
};

export const addS3Link = async (songid: string, s3link: string) => {
  const res = await axios.post(`${content_base_url}/tracks/${songid}/addS3Link/${s3link}`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const findSongsByFileNames = async (userid: string, names: string[]) => {
  const res = await axios.post<{
    query: string,
    results: SongType[]
  }[]>(`${content_base_url}/tracks/findmultiplebynames/${userid}`, {
    names
  });
  if (res.status === 201) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const findSongsByIds = async (userid: string, ids: string[]) => {
  const res = await axios.post<{
    query: string,
    results: SongType[]
  }[]>(`${content_base_url}/tracks/findmultiplebyids/${userid}`, {
    ids
  });
  if (res.status === 201) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const findSongByName = async (userid: string, names: string) => {
  const res = await axios.post<SongType[]>(`${content_base_url}/tracks/findbyname/${userid}`, {
    names
  });
  if (res.status === 201) {
    return res.data;
  }
  throw new Error('Network response not ok');
};
function chunk(arr: any, len: number) {

  var chunks = [],
    i = 0,
    n = arr.length;

  while (i < n) {
    chunks.push(arr.slice(i, i += len));
  }

  return chunks;
}

export const uploadMultipleSongs = async (songfiles: { song: any, file: File }[]) => {
  const response: { status: 1 | 2, message: string }[] = []
  for (const songFile of songfiles) {
    const formData = new FormData();
    formData.append("audiofiles", songFile.file);
    await axios.post(`${content_base_url}/stream/uploadAll/${songFile.song.id}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }).then(res => {
      response.push({ status: 1, message: res.data })
    }).catch(err => {
      response.push({ status: 2, message: err })
    })
  }
  return response
};


export const uploadImage = async (file: File) => {
  const formData = new FormData();
  formData.append("image", file);
  const res2 = await axios.post(`${content_base_url}/playlist/uploadImage/`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
  if (res2.status == 201) {
    return res2.data
  }
  throw new Error('Network response not ok');
};

export interface abcTYpe2 extends SongType {
  filename: string,
  s3link: string,
  query: string
}

export const searchAll = async (query: string, type: SearchTypeEnum | undefined, page: number) => {
  if (!query) return undefined
  const res = await axios.get<SearchPageResType>(`${search_base_url}/search/${'test'}/${query}/${type || 'null'}/${page}`);
  if (res.status == 200) {
    return res.data
  }
  throw new Error('Network response not ok');
};

export const requestSpotifysearch = async (userid: string, query: string, page: number) => {
  const res = await axios.get<SearchType>(`${content_base_url}/search/${userid}/${query}/${''}/${page}`);
  if (res.status == 200) {
    return res.data
  }
  throw new Error('Network response not ok');
};

export const requestTrackAddition = async (userid: string, songid: string) => {
  return axios.post<{
    id: string, name: string, album: string, artist: string
  }>(`${content_base_url}/tracks/requestTrackAddition/${userid}/${songid}`).then(res => {
    axios.post('http://104.197.192.67/spotify_downloader/api/download_song', {
      id: songid,
      name: res.data.name,
      album: res.data.album,
      artist: res.data.artist
    }).then(res2 => {
      return 'success'
    }).catch(error => {
      console.error(`Error in making call for coversion calls ${error}`)
      return 'failed'
    })
  }).catch(err => {
    console.error(`Error in request track addition ${err}`)
    return 'failed2'
  })
};


export const requestPodcastEpisodesPopulation = async (userid: string, songid: string) => {
  const res = await axios.post(`${content_base_url}/podcast/requestPodcastEpisodesPopulation/${userid}/${songid}`);
  if (res.status == 201) {
    return res.data
  }
  throw new Error('Network response not ok');
};

// Playlist
export const createNewPlaylist = async (userid: string, name: string, image: string) => {
  const res = await axios.post(`${content_base_url}/playlist/${userid}/create-new-playlist/${name}`, {
    image
  });
  if (res.status == 201) {
    return res.data
  }
  throw new Error('Network response not ok');
};

export const addSongToPlaylist = async (userid: string, playlists: string[], songid: string, posterpath: string): Promise<PlayListType> => {
  const res = await axios.put<PlayListType>(`${content_base_url}/playlist/${userid}/${songid}`, { playlists, posterpath });
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const getUserPlaylist = async (userid: string): Promise<UserPlayListType[]> => {
  if (!userid) return []
  const res = await axios.get<UserPlayListType[]>(`${content_base_url}/playlist/${userid}/playlists`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const getFav = async (user_id: string): Promise<PlayListType | undefined> => {
  if (!user_id) return undefined
  const res = await axios.get<PlayListType>(`${content_base_url}/playlist/${user_id}/fav`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const deletePlayList = async (userid: string, playlistid: String) => {
  const res = await axios.delete(`${content_base_url}/playlist/${userid}/${playlistid}`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const getStreamLink = async (s3link: string, userid: string, id: string) => {
  const res = await axios.post(`${content_base_url}/stream/`, { userid: userid, id: id, s3link: s3link });
  if (res.status === 201) {
    return res.data;
  }
  throw new Error('Network response not ok');
};
export const getAllAvailableSongs = async (skip: number, limit: number) => {
  const res = await axios.get<AllTracksPageInfo>(`${content_base_url}/stream/all_available_songs/${skip}/${limit}`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const getAllSongs = async (page: number) => {
  const res = await axios.get<any[]>(`${content_base_url}/stream/all_songs/${page}`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export type AllTracksPageInfo = {
  results: SongType[],
  page: number
}


export const markAsBadLink = async (id: string, bad: boolean) => {
  const res = await axios.post(`${content_base_url}/stream/markbadlink/${id}/${bad}`);
  if (res.status === 201) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const getAllBadLink = async () => {
  const res = await axios.get<{ id: string, bad: boolean }[]>(`${content_base_url}/stream/allbadlink`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};



// Radio 
export const userLikedStatison = async (userid: string) => {
  if (!userid) return []
  const res = await axios.get(`${content_base_url}/radio/${userid}/liked_stations`);
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};


export const getTop10StationsByGenres = async (countryCode: string, genre: string) => {
  const res = await axios.get(`${content_base_url}/radio/${countryCode}/getTop10StationsByGenres/${genre}`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};


export const browseStationsByCountryAndGenre = async (countryCode: string, genre: string, page: number) => {
  if (!countryCode) return {
    page: page,
    results: []
  }
  const res = await axios.get<RadioStationsPageInfo>(`${content_base_url}/radio/${countryCode}/browseStationsByCountryAndGenre/${page}?genre=${genre}`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};
export type RadioStationsPageInfo = {
  results: RadioStation[],
  page: number
}

export const browseStationsByCountry = async (countryCode: string, page: number) => {
  if (!countryCode) return {
    page: page,
    results: []
  }
  const res = await axios.get<RadioStationsPageInfo>(`${content_base_url}/radio/${countryCode}/browseStationsByCountry/${page}`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};




export const trendingStations = async (countryCode: string) => {
  const res = await axios.get<RadioStation[]>(`${content_base_url}/radio/${countryCode}/trendingStations`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};
export const getLastPlayedStations = async (userid: string) => {
  if (!userid) return []
  const res = await axios.get<RadioStation[]>(`${content_base_url}/radio/${userid}/last_played_stations`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};




export const userListenedStation = async (userid: string, stationid: string, duration: number) => {
  const res = await axios.put(`${content_base_url}/radio/${userid}/listened_station/${stationid}/${duration}`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const getAllRadioCountries = async () => {
  const res = await axios.get<{
    name: string;
    count: number;
    code: string
  }[]>(`${content_base_url}/radio/all_countries`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const getAllGenres = async (countryCode: string) => {
  const res = await axios.get<MeiliSearchAggType[]>(`${content_base_url}/radio/all_genres/${countryCode}`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const fetchRadio = async (id: string) => {
  const res = await axios.get<RadioStationType>(`${content_base_url}/radio/${id}`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

// Podcasts
export const getAllPodcastGenres = async () => {
  const res = await axios.get<MeiliSearchAggType[]>(`${content_base_url}/podcast/all_genres`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const getUserLikedPodcasts = async (userid: string) => {
  const res = await axios.get<PodcastCardDto[]>(`${content_base_url}/podcast/:userid/liked_podcasts`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const getTop10PodcastsByGenres = async (genre: string) => {
  const res = await axios.get<PodcastCardDto[]>(`${content_base_url}/podcast/getTop10PodcastsByGenres/${genre}`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};
export type BrowsePodcastsPageInfo = {
  results: PodcastsCardCarousel[],
  page: number
}
export type PodcastsCardCarousel = {
  cards: PodcastCardDto[],
  label: string
}

// export const browseTop10Podcasts = async (page: number) => {
//   const res = await axios.get<BrowsePodcastsPageInfo>(`${content_base_url}/podcast/browseTop10PodcastsByGenres/${page}`)
//   if (res.status === 200) {
//     return res.data;
//   }
//   throw new Error('Network response not ok');
// };

export const browsePodcastsByGenre = async (genre: string, page: number) => {
  const res = await axios.get<PodcastsPageInfo>(`${content_base_url}/podcast/browsePodcastsByGenre/${genre}/${page}`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const browsePodcasts = async (page: number) => {
  const res = await axios.get<PodcastsPageInfo>(`${content_base_url}/podcast/browse/${page}`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const userLikedPodacsts = async (userid: string, podcastid: string, episodeid: string) => {
  const res = await axios.put(`${content_base_url}/podcast/${userid}/liked_podcast/${podcastid}/${episodeid}`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const userListenedPodcasts = async (userid: string, podcastid: string, duration: number, episodeid: string) => {
  const res = await axios.put<PodcastCardDto[]>(`${content_base_url}/podcast/${userid}/listened_station/${podcastid}/${episodeid}/${duration}`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};
export type PodcastsEpisodesPageInfo = {
  results: PodcastEpisodeDto[],
  page: number
}
export const getPodcastEpisoes = async (id: string, page: number) => {
  const res = await axios.get<PodcastsEpisodesPageInfo>(`${content_base_url}/podcast/${id}/episodes/${page}`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const getPodcastDetails = async (id: string) => {
  const res = await axios.get<PodcastCardDto>(`${content_base_url}/podcast/${id}`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};
export const topPodcastsByUserActivity = async (id: string) => {
  const res = await axios.get<PodcastCardDto[]>(`${content_base_url}/podcast/${id}/topPodcastsByUserActivity`)
  if (res.status === 200) {
    return res.data;
  }
  throw new Error('Network response not ok');
};

export const songGenres = async (id: string) => {
  const res = await axios.get<{ value: string, count: number }[]>(`${content_base_url}/search/${id}/song_genres`)
  if (res.status === 200) {
    return res.data?.filter(x => x.value.length > 3)?.sort((a, b) => b.count - a.count);
  }
  throw new Error('Network response not ok');
};


export const requestPlaylistTracksAddition = async (userid: string, pid: number) => {
  axios.get(`${addition_base_url}/findAndSendSongsToSavedMessage/${pid}`).then(res => res).catch(err => {
    throw new Error('Network response not ok', err);
  })
};