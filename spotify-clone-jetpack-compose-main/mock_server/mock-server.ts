import express, { Request, Response, NextFunction } from 'express';
import cors from 'cors';
import { v4 as uuidv4 } from 'uuid';

const app = express();
const PORT = 9000;

app.use(cors());
app.use(express.json());

// ==========================================
// TYPES (Enum as const for strip-only compatibility)
// ==========================================

export const SearchTypeEnum = {
  album: 'album',
  songs: 'songs',
  artist: 'artist',
  radio: 'radio',
  podcast: 'podcast'
} as const;
export type SearchTypeEnumType = typeof SearchTypeEnum[keyof typeof SearchTypeEnum];

export type SongType = {
  id: string;
  name: string;
  playlist_id: string;
  playlist_name: string;
  artists: { title: string; id: string; path: string }[];
  duration: number;
  likes: number;
  genres: string[];
  album: { title: string; id: string; path: string };
  thumbnail: string;
  view_count: number;
  preview_url: string;
  s3link: string;
};

export type SearchPageResType = {
  total: number;
  took: number;
  cards: { play_url: string; id: string; image: string; name: string; artist: string; type: SearchTypeEnumType }[];
  type: SearchTypeEnumType;
};

export type UserPlayListType = {
  id: string;
  name: string;
  image: string;
  tracks: string[];
  user_id: string;
  followers: string[];
  created_at: string;
  updated_at: string;
};

export type UserType = {
  id: string;
  name: string;
  playlists: UserPlayListType[];
  liked_songs: string[];
  liked_podcast: string[];
  liked_radio: string[];
  tracks: string[];
  artists: string[];
};

export type CardContentType = {
  image: string;
  id: string;
  title: string;
  subtitle: string;
  type: 'playlist_card' | 'music_card' | 'artists_card';
  path: string;
  song: SongType | null;
};

export type HomePageDataType = {
  cardType: string;
  label: string;
  path: string;
  cards: CardContentType[];
  id: string;
};

export type PlayListType = {
  id: number;
  image: string;
  title: string;
  artists: { id: string; name: string }[];
  songs: SongType[];
};

export type AllPlayListType = {
  image: string;
  id: string;
  title: string;
};

export type ArtistListingType = {
  image: string;
  id: string;
  title: string;
};

export type ArtistType = {
  image: string;
  image_type: string;
  id: string;
  title: string;
  monthly_listeners: number;
  popular_songs: SongType[];
  featured_albums: HomePageDataType;
  popular_releases: HomePageDataType;
  singles: HomePageDataType;
  albums_featuring_artist: HomePageDataType;
  fans_also_like: HomePageDataType;
  appears_on: HomePageDataType;
  dicovered_on: HomePageDataType;
  genres: string[];
  similar: ArtistListingType[];
};

export type PodcastCardDto = {
  id: string;
  image: string;
  title: string;
  publisher: string;
};

export type PodcastEpisodeDto = {
  id: string;
  name: string;
  summary: string;
  thumbnail: string;
  duration: number;
};

export type RadioStation = {
  id: string;
  clickCount: number;
  countryCode: string;
  favicon: string;
  name: string;
  tags: string[];
  url: string;
  created_at: string;
};

export type MeiliSearchAggType = { value: string; count: number };

// ==========================================
// HELPERS
// ==========================================

const rand = (min: number, max: number) => Math.floor(Math.random() * (max - min + 1) + min);
const uuid = () => uuidv4();
// Force bitmap format; Glide may fail decoding SVG/auto-negotiated formats from placeholder services.
const img = (t: string) => `https://placehold.co/150x150/png?text=${encodeURIComponent(t)}`;

const makeSong = (): SongType => ({
  id: uuid(),
  name: `Song ${uuid().slice(0, 6)}`,
  playlist_id: uuid(),
  playlist_name: `PL ${uuid().slice(0, 4)}`,
  artists: [{ title: `Artist ${uuid().slice(0, 4)}`, id: uuid(), path: '/artist' }],
  duration: rand(120, 300),
  likes: rand(0, 10000),
  genres: ['Pop', 'Rock', 'Jazz'],
  album: { title: `Album ${uuid().slice(0, 4)}`, id: uuid(), path: '/album' },
  thumbnail: img('Song'),
  view_count: rand(0, 100000),
  preview_url: 'https://example.com/preview.mp3',
  s3link: 'https://s3.example.com/song.mp3',
});

const makePlaylist = (): PlayListType => ({
  id: rand(1, 99999),
  image: img('Playlist'),
  title: `Playlist ${uuid().slice(0, 6)}`,
  artists: [{ id: uuid(), name: `Artist ${uuid().slice(0, 4)}` }],
  songs: Array.from({ length: rand(1, 5) }, makeSong), // ✅ Always has songs
});

const makeHomePageData = (): HomePageDataType => ({
  cardType: 'section',
  label: `Section ${uuid().slice(0, 4)}`,
  path: '/section',
  cards: Array.from({ length: rand(2, 4) }, () => ({
    image: img('Card'),
    id: uuid(),
    title: `Title ${uuid().slice(0, 4)}`,
    subtitle: `Sub ${uuid().slice(0, 3)}`,
    type: ['playlist_card', 'music_card', 'artists_card'][rand(0, 2)] as any,
    path: '/details',
    song: rand(0, 1) ? makeSong() : null,
  })),
  id: uuid(),
});

const makeArtist = (): ArtistType => ({
  image: img('Artist'),
  image_type: 'png',
  id: uuid(),
  title: `Artist ${uuid().slice(0, 6)}`,
  monthly_listeners: rand(1000, 1000000),
  popular_songs: Array.from({ length: 3 }, makeSong),
  featured_albums: makeHomePageData(),
  popular_releases: makeHomePageData(),
  singles: makeHomePageData(),
  albums_featuring_artist: makeHomePageData(),
  fans_also_like: makeHomePageData(),
  appears_on: makeHomePageData(),
  dicovered_on: makeHomePageData(),
  genres: ['Rock', 'Pop'],
  similar: Array.from({ length: 2 }, () => ({
    image: img('Similar'),
    id: uuid(),
    title: `Similar ${uuid().slice(0, 4)}`,
  })),
});

const makeRadio = (): RadioStation => ({
  id: uuid(),
  clickCount: rand(0, 5000),
  countryCode: 'US',
  favicon: img('Favicon'),
  name: `Radio ${uuid().slice(0, 6)}`,
  tags: ['News', 'Music'],
  url: 'https://stream.example.com',
  created_at: new Date().toISOString(),
});

const makePodcastCard = (): PodcastCardDto => ({
  id: uuid(),
  image: img('Podcast'),
  title: `Podcast ${uuid().slice(0, 6)}`,
  publisher: `Publisher ${uuid().slice(0, 4)}`,
});

const makePodcastEpisode = (): PodcastEpisodeDto => ({
  id: uuid(),
  name: `Episode ${uuid().slice(0, 4)}`,
  summary: 'Episode summary',
  thumbnail: img('Episode'),
  duration: rand(1000, 3000),
});

const success = (res: Response, code = 200) => res.status(code).json({ status: 'success' });

// Logging middleware for debugging
app.use((req: Request, res: Response, next: NextFunction) => {
  console.log(`🔍 ${req.method} ${req.path}`);
  next();
});

// ==========================================
// ROUTES - WITH EXPLICIT ARRAY/SINGLE RETURNS
// ==========================================

// --- USERS ---
app.get('/api/spotify/users/:id', (req, res) => {
  const user: UserType = {
    id: req.params.id,
    name: 'Test User',
    playlists: [],
    liked_songs: [],
    liked_podcast: [],
    liked_radio: [],
    tracks: [],
    artists: [],
  };
  res.json(user); // ✅ SINGLE UserType
});

app.get('/api/spotify/users/:id/home/:page', (req, res) => {
  res.json({
    page: parseInt(req.params.page),
    results: Array.from({ length: 3 }, makeHomePageData), // ✅ HomePageDataType[]
  });
});

// --- PLAYLISTS & ALBUMS ---
app.get('/api/spotify/playlist/:id', (req, res) => {
  console.log('📦 /playlist/:id returning SINGLE PlayListType');
  res.json(makePlaylist()); // ✅ SINGLE PlayListType
});

// ⚠️ KEY FIX: This MUST return an ARRAY
app.get('/api/spotify/album/freshAlbums', (req, res) => {
  const result: PlayListType[] = Array.from({ length: 5 }, makePlaylist);
  console.log('📦 /album/freshAlbums returning ARRAY with', result.length, 'items');
  console.log('📦 First item type:', Array.isArray(result) ? 'ARRAY' : 'OBJECT');
  res.json(result); // ✅ PlayListType[]
});
app.get('/api/spotify/album/:id', (req, res) => {
  console.log('📦 /album/:id returning SINGLE PlayListType');
  res.json(makePlaylist()); // ✅ SINGLE PlayListType
});



app.get('/api/spotify/playlist_collection/top/10/0', (req, res) => {
  const result: PlayListType[] = Array.from({ length: 10 }, makePlaylist);
  console.log('📦 /playlist_collection/top/10/0 returning ARRAY');
  res.json(result); // ✅ PlayListType[]
});

app.get('/api/spotify/playlist_collection/:id', (req, res) => {
  console.log('📦 /playlist_collection/:id returning SINGLE');
  res.json(makePlaylist()); // ✅ SINGLE PlayListType
});

app.get('/api/spotify/playlist_collection/:userid/liked_plc', (req, res) => {
  console.log('📦 /playlist_collection/:userid/liked_plc returning SINGLE HomePageDataType');
  res.json(makeHomePageData()); // ✅ SINGLE HomePageDataType
});

app.get('/api/spotify/playlist_collection/getTopFollowingPlayListCollections', (req, res) => {
  res.json(makeHomePageData()); // ✅ SINGLE
});
app.get('/api/spotify/playlist_collection/getEditorsPlayList/:limit', (req, res) => {
  res.json(makeHomePageData()); // ✅ SINGLE
});
app.get('/api/spotify/playlist_collection/getLatestPlayListCollections', (req, res) => {
  res.json(makeHomePageData()); // ✅ SINGLE
});
app.get('/api/spotify/playlist_collection/:userid/listened_plc/last_played', (req, res) => {
  res.json(makeHomePageData()); // ✅ SINGLE
});

app.put('/api/spotify/playlist_collection/:userid/liked_plc/:stationid', (req, res) => success(res));
app.put('/api/spotify/playlist_collection/:userid/listened_plc/:stationid/:duration', (req, res) => success(res));

// --- TRACKS ---
app.get('/api/spotify/tracks/:userid/queue', (req, res) => {
  const result: SongType[] = Array.from({ length: 5 }, makeSong);
  console.log('🎵 /tracks/:userid/queue returning ARRAY');
  res.json(result); // ✅ SongType[]
});

app.get('/api/spotify/tracks/top_scoring_songs/:userid/:limit', (req, res) => {
  res.json(Array.from({ length: parseInt(req.params.limit) || 5 }, makeSong)); // ✅ SongType[]
});
app.get('/api/spotify/tracks/top_scoring_songs/:limit', (req, res) => {
  res.json(Array.from({ length: parseInt(req.params.limit) || 5 }, makeSong)); // ✅ SongType[]
});

app.get('/api/spotify/tracks/:userid/next_item_in_queue', (req, res) => {
  res.json({ song: makeSong(), updated_queue: Array.from({ length: 3 }, makeSong) });
});
app.get('/api/spotify/tracks/:userid/prev_item_in_queue', (req, res) => {
  res.json({ song: makeSong(), updated_queue: Array.from({ length: 3 }, makeSong) });
});

app.get('/api/spotify/tracks/:id', (req, res) => {
  console.log('🎵 /tracks/:id returning SINGLE SongType');
  res.json(makeSong()); // ✅ SINGLE SongType
});

app.delete('/api/spotify/tracks/:id', (req, res) => res.send('deleted'));

app.put('/api/spotify/tracks/:id/:trackid', (req, res) => success(res));
app.post('/api/spotify/tracks/:id/add-multiple', (req, res) => success(res, 201));
app.delete('/api/spotify/tracks/:id/:trackid', (req, res) => success(res));

app.put('/api/spotify/users/:id/likedislike/song/:trackid/:likedislike', (req, res) => success(res));
app.put('/api/spotify/users/:id/likedislike/radio/:trackid/:likedislike', (req, res) => success(res));
app.put('/api/spotify/users/:id/likedislike/podcast/:trackid/:likedislike', (req, res) => success(res));

app.post('/api/spotify/tracks/:songid/addS3Link/:s3link', (req, res) => success(res));

app.post('/api/spotify/tracks/findmultiplebynames/:userid', (req, res) => {
  res.status(201).json([{ query: 'test', results: Array.from({ length: 2 }, makeSong) }]);
});
app.post('/api/spotify/tracks/findmultiplebyids/:userid', (req, res) => {
  res.status(201).json([{ query: 'test', results: Array.from({ length: 2 }, makeSong) }]);
});
app.post('/api/spotify/tracks/findbyname/:userid', (req, res) => {
  res.status(201).json(Array.from({ length: 2 }, makeSong)); // ✅ SongType[]
});

app.post('/api/spotify/stream/uploadAll/:id', (req, res) => success(res, 201));
app.post('/api/spotify/stream/', (req, res) => success(res, 201));

app.get('/api/spotify/stream/all_available_songs/:skip/:limit', (req, res) => {
  res.json({ page: 1, results: Array.from({ length: parseInt(req.params.limit) || 10 }, makeSong) });
});
app.get('/api/spotify/stream/all_songs/:page', (req, res) => {
  res.json(Array.from({ length: 10 }, makeSong)); // ✅ any[]
});

app.post('/api/spotify/stream/markbadlink/:id/:bad', (req, res) => success(res, 201));
app.get('/api/spotify/stream/allbadlink', (req, res) => {
  res.json([{ id: uuid(), bad: true }]);
});

app.post('/api/spotify/tracks/requestTrackAddition/:userid/:songid', (req, res) => {
  res.json({ id: req.params.songid, name: 'Track', album: 'Album', artist: 'Artist' });
});

// --- ARTISTS ---
app.get('/api/spotify/artist/songs/:id/:page', (req, res) => {
  res.json({ page: parseInt(req.params.page), results: Array.from({ length: 5 }, makeSong) });
});

app.get('/api/spotify/artist/top', (req, res) => {
  const result: ArtistType[] = Array.from({ length: 5 }, makeArtist);
  console.log('🎤 /artist/top returning ARRAY');
  res.json(result); // ✅ ArtistType[]
});

app.get('/api/spotify/artist/:id/playlists/:limit/:skip', (req, res) => {
  res.json(Array.from({ length: parseInt(req.params.limit) || 5 }, makePlaylist)); // ✅ PlayListType[]
});
app.get('/api/spotify/artist/:id/playlists_collection/:limit/:skip', (req, res) => {
  res.json(Array.from({ length: parseInt(req.params.limit) || 5 }, makePlaylist)); // ✅ PlayListType[]
});

app.get('/api/spotify/artist/:id', (req, res) => {
  console.log('🎤 /artist/:id returning SINGLE ArtistType');
  res.json(makeArtist()); // ✅ SINGLE ArtistType
});

app.get('/api/spotify/artist/all/:page', (req, res) => {
  res.json({
    page: parseInt(req.params.page),
    results: Array.from({ length: 5 }, (): ArtistListingType => ({
      image: img('Artist'),
      id: uuid(),
      title: `Artist ${uuid().slice(0, 4)}`,
    })), // ✅ ArtistListingType[]
  });
});

app.get('/api/spotify/album/all/:page', (req, res) => {
  res.json({
    page: parseInt(req.params.page),
    results: Array.from({ length: 5 }, (): ArtistListingType => ({
      image: img('Album'),
      id: uuid(),
      title: `Album ${uuid().slice(0, 4)}`,
    })), // ✅ ArtistListingType[]
  });
});

// --- PLAYLIST COLLECTIONS (Pagination) ---
app.get('/api/spotify/playlist_collection/all/:page', (req, res) => {
  res.json({
    page: parseInt(req.params.page),
    results: Array.from({ length: 5 }, (): AllPlayListType => ({
      image: img('Collection'),
      id: uuid(),
      title: `Collection ${uuid().slice(0, 4)}`,
    })), // ✅ AllPlayListType[]
  });
});

app.get('/api/spotify/playlist_collection/browseByGenre/:page', (req, res) => {
  res.json({
    page: parseInt(req.params.page),
    results: Array.from({ length: 5 }, (): AllPlayListType => ({
      image: img('Genre'),
      id: uuid(),
      title: `Genre ${uuid().slice(0, 4)}`,
    })),
  });
});

app.get('/api/spotify/playlist_collection/browseTop10PlaylistCollectionByGenre/:userid/:page', (req, res) => {
  res.json({
    page: parseInt(req.params.page),
    results: Array.from({ length: 3 }, makeHomePageData), // ✅ HomePageDataType[]
  });
});

// --- SEARCH ---
app.get('/api/spotify/search/:userid/plc_genres', (req, res) => {
  res.json([{ value: 'Pop', count: 10 }, { value: 'Rock', count: 5 }]); // ✅ MeiliSearchAggType[]
});

app.get('/api/spotify/search/:test/:query/:type/:page', (req, res) => {
  res.json({
    total: 100,
    took: 10,
    cards: Array.from({ length: 5 }, () => ({
      play_url: 'https://example.com/play',
      id: uuid(),
      image: img('Search'),
      name: 'Result',
      artist: 'Artist',
      type: (req.params.type || 'songs') as SearchTypeEnumType,
    })),
    type: (req.params.type || 'songs') as SearchTypeEnumType,
  });
});

app.get('/api/spotify/search/:userid/:query/:empty/:page', (req, res) => {
  res.json({
    total: 100,
    took: 10,
    cards: Array.from({ length: 5 }, () => ({
      play_url: 'https://example.com/play',
      id: uuid(),
      image: img('Search'),
      name: 'Result',
      artist: 'Artist',
      type: 'songs' as SearchTypeEnumType,
    })),
    type: 'songs' as SearchTypeEnumType,
  });
});

app.get('/api/spotify/search/:id/song_genres', (req, res) => {
  res.json(
    [{ value: 'Pop', count: 100 }, { value: 'Rock', count: 50 }]
      .filter((x) => x.value.length > 3)
      .sort((a, b) => b.count - a.count)
  );
});

// --- USER PLAYLISTS ---
app.post('/api/spotify/playlist/:userid/create-new-playlist/:name', (req, res) => {
  res.status(201).json({
    id: uuid(),
    name: req.params.name,
    image: img('NewPL'),
    tracks: [],
    user_id: req.params.userid,
    followers: [],
    created_at: new Date().toISOString(),
    updated_at: new Date().toISOString(),
  } as UserPlayListType);
});

app.put('/api/spotify/playlist/:userid/:songid', (req, res) => {
  res.json(makePlaylist()); // ✅ PlayListType
});

app.get('/api/spotify/playlist/:userid/playlists', (req, res) => {
  const result: UserPlayListType[] = Array.from({ length: 5 }, () => ({
    id: uuid(),
    name: `Playlist ${uuid().slice(0, 4)}`,
    image: img('UserPL'),
    tracks: [uuid(), uuid()],
    user_id: req.params.userid,
    followers: [uuid()],
    created_at: new Date().toISOString(),
    updated_at: new Date().toISOString(),
  }));
  console.log('📋 /playlist/:userid/playlists returning ARRAY');
  res.json(result); // ✅ UserPlayListType[]
});

app.get('/api/spotify/playlist/:user_id/fav', (req, res) => {
  console.log('📋 /playlist/:user_id/fav returning SINGLE');
  res.json(makePlaylist()); // ✅ SINGLE PlayListType
});

app.delete('/api/spotify/playlist/:userid/:playlistid', (req, res) => success(res));

app.put('/api/spotify/users/:id/:playlistid/follow-playlist', (req, res) => success(res));
app.put('/api/spotify/users/:id/:playlistid/unfollow-playlist', (req, res) => success(res));

app.post('/api/spotify/playlist/uploadImage/', (req, res) => {
  res.status(201).json({ url: 'https://s3.example.com/image.jpg' });
});

// --- RADIO ---
app.get('/api/spotify/radio/:userid/liked_stations', (req, res) => {
  const result: RadioStation[] = Array.from({ length: 5 }, makeRadio);
  console.log('📻 /radio/:userid/liked_stations returning ARRAY');
  res.json(result); // ✅ RadioStation[]
});

app.get('/api/spotify/radio/:countryCode/getTop10StationsByGenres/:genre', (req, res) => {
  res.json(Array.from({ length: 10 }, makeRadio)); // ✅ RadioStation[]
});

app.get('/api/spotify/radio/:countryCode/browseStationsByCountryAndGenre/:page', (req, res) => {
  res.json({ page: parseInt(req.params.page), results: Array.from({ length: 5 }, makeRadio) });
});
app.get('/api/spotify/radio/:countryCode/browseStationsByCountry/:page', (req, res) => {
  res.json({ page: parseInt(req.params.page), results: Array.from({ length: 5 }, makeRadio) });
});

app.get('/api/spotify/radio/:countryCode/trendingStations', (req, res) => {
  res.json(Array.from({ length: 10 }, makeRadio)); // ✅ RadioStation[]
});

app.get('/api/spotify/radio/:userid/last_played_stations', (req, res) => {
  res.json(Array.from({ length: 5 }, makeRadio)); // ✅ RadioStation[]
});

app.put('/api/spotify/radio/:userid/listened_station/:stationid/:duration', (req, res) => success(res));

app.get('/api/spotify/radio/all_countries', (req, res) => {
  res.json([
    { name: 'United States', count: 100, code: 'US' },
    { name: 'United Kingdom', count: 50, code: 'UK' },
  ]);
});

app.get('/api/spotify/radio/all_genres/:countryCode', (req, res) => {
  res.json([{ value: 'News', count: 10 }, { value: 'Music', count: 20 }]); // ✅ MeiliSearchAggType[]
});

app.get('/api/spotify/radio/:id', (req, res) => {
  console.log('📻 /radio/:id returning SINGLE');
  res.json(makeRadio()); // ✅ SINGLE RadioStation
});

// --- PODCASTS ---
app.get('/api/spotify/podcast/all_genres', (req, res) => {
  res.json([{ value: 'Tech', count: 10 }, { value: 'Comedy', count: 20 }]);
});

app.get('/api/spotify/podcast/:userid/liked_podcasts', (req, res) => {
  const result: PodcastCardDto[] = Array.from({ length: 5 }, makePodcastCard);
  console.log('🎙️ /podcast/:userid/liked_podcasts returning ARRAY');
  res.json(result); // ✅ PodcastCardDto[]
});

app.get('/api/spotify/podcast/getTop10PodcastsByGenres/:genre', (req, res) => {
  res.json(Array.from({ length: 10 }, makePodcastCard)); // ✅ PodcastCardDto[]
});

app.get('/api/spotify/podcast/browsePodcastsByGenre/:genre/:page', (req, res) => {
  res.json({
    page: parseInt(req.params.page),
    results: Array.from({ length: 3 }, () => ({
      cards: Array.from({ length: 5 }, makePodcastCard),
      label: 'Genre Section',
    })),
  });
});
export type PodcastsPageInfo = {
    results: PodcastCardDto[],
    page: number
}
app.get('/api/spotify/podcast/browse/:page', (req, res) => {
    let result:PodcastsPageInfo={
        results:Array.from({ length: 5 }, makePodcastCard),
        page: 0
    }
  res.json(result);
});

app.put('/api/spotify/podcast/:userid/liked_podcast/:podcastid/:episodeid', (req, res) => success(res));

app.put('/api/spotify/podcast/:userid/listened_station/:podcastid/:episodeid/:duration', (req, res) => {
  res.json(Array.from({ length: 3 }, makePodcastCard)); // ✅ PodcastCardDto[]
});

app.get('/api/spotify/podcast/:id/episodes/:page', (req, res) => {
  res.json({ page: parseInt(req.params.page), results: Array.from({ length: 5 }, makePodcastEpisode) });
});

app.get('/api/spotify/podcast/:id', (req, res) => {
  console.log('🎙️ /podcast/:id returning SINGLE');
  res.json(makePodcastCard()); // ✅ SINGLE PodcastCardDto
});

app.get('/api/spotify/podcast/:id/topPodcastsByUserActivity', (req, res) => {
  res.json(Array.from({ length: 5 }, makePodcastCard)); // ✅ PodcastCardDto[]
});

app.post('/api/spotify/podcast/requestPodcastEpisodesPopulation/:userid/:songid', (req, res) => {
  success(res, 201);
});

// --- ADDITION SERVICE ---
app.get('/findAndSendSongsToSavedMessage/:pid', (req, res) => success(res));

// --- CATCH-ALL ---
app.use((req, res) => {
  console.log(`❌ 404: ${req.method} ${req.path}`);
  res.status(404).json({ error: 'Endpoint not mocked' });
});

// --- START ---
app.listen(PORT, () => {
  console.log('\n✅ Mock server: http://localhost:' + PORT);
  console.log('📍 Update index.ts:');
  console.log('   content_base_url = "http://localhost:3000/api/spotify"');
  console.log('   addition_base_url = "http://localhost:3000"');
  console.log('   Expected output: 5\n');
});