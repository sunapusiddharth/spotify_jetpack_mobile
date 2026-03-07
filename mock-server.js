const express = require('express');
const cors = require('cors');
const { v4: uuidv4 } = require('uuid');

const app = express();
const PORT = 9000;

app.use(cors());
app.use(express.json());

// Helper to generate consistent mock data
const rand = (min, max) => Math.floor(Math.random() * (max - min + 1) + min);
const uuid = () => uuidv4();
const img = (text) => `https://via.placeholder.com/150?text=${text}`;

const makeSong = () => ({
  id: uuid(),
  name: `Song ${uuid().slice(0,6)}`,
  playlist_id: uuid(),
  playlist_name: `PL ${uuid().slice(0,4)}`,
  artists: [{ title: `Artist ${uuid().slice(0,4)}`, id: uuid(), path: '/artist' }],
  duration: rand(120, 300),
  likes: rand(0, 10000),
  genres: ['Pop', 'Rock', 'Jazz'][rand(0,2)],
  album: { title: `Album ${uuid().slice(0,4)}`, id: uuid(), path: '/album' },
  thumbnail: img('Song'),
  view_count: rand(0, 100000),
  preview_url: 'https://example.com/preview.mp3',
  s3link: 'https://s3.example.com/song.mp3'
});

const makePlaylist = () => ({
  id: rand(1, 99999),
  image: img('Playlist'),
  title: `Playlist ${uuid().slice(0,6)}`,
  artists: [{ id: uuid(), name: `Artist ${uuid().slice(0,4)}` }],
  songs: Array.from({ length: rand(1,5) }, makeSong) // ✅ Ensure songs array is populated
});

const makeHomePageData = () => ({
  cardType: 'section',
  label: `Section ${uuid().slice(0,4)}`,
  path: '/section',
  cards: Array.from({ length: rand(2,4) }, () => ({
    image: img('Card'),
    id: uuid(),
    title: `Title ${uuid().slice(0,4)}`,
    subtitle: `Sub ${uuid().slice(0,3)}`,
    type: ['playlist_card','music_card','artists_card'][rand(0,2)],
    path: '/details',
    song: rand(0,1) ? makeSong() : null
  })),
  id: uuid()
});

const makeArtist = () => ({
  image: img('Artist'),
  image_type: 'png',
  id: uuid(),
  title: `Artist ${uuid().slice(0,6)}`,
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
    title: `Similar ${uuid().slice(0,4)}`
  }))
});

const makeRadio = () => ({
  id: uuid(),
  clickCount: rand(0,5000),
  countryCode: 'US',
  favicon: img('Favicon'),
  name: `Radio ${uuid().slice(0,6)}`,
  tags: ['News', 'Music'],
  url: 'https://stream.example.com',
  created_at: new Date().toISOString()
});

const makePodcastCard = () => ({
  id: uuid(),
  image: img('Podcast'),
  title: `Podcast ${uuid().slice(0,6)}`,
  publisher: `Publisher ${uuid().slice(0,4)}`
});

const makePodcastEpisode = () => ({
  id: uuid(),
  name: `Episode ${uuid().slice(0,4)}`,
  summary: 'Episode summary',
  thumbnail: img('Episode'),
  duration: rand(1000, 3000)
});

const success = (res, code = 200) => res.status(code).json({ status: 'success' });

// ==================== ROUTES ====================

// USERS
app.get('/api/spotify/users/:id', (req, res) => res.json({
  id: req.params.id,
  name: 'Test User',
  playlists: [],
  liked_songs: [],
  liked_podcast: [],
  liked_radio: [],
  tracks: [],
  artists: []
}));

app.get('/api/spotify/users/:id/home/:page', (req, res) => res.json({
  page: parseInt(req.params.page),
  results: Array.from({ length: 3 }, makeHomePageData) // ✅ HomePageDataType[]
}));

// PLAYLISTS & ALBUMS - KEY FIXES HERE
app.get('/api/spotify/playlist/:id', (req, res) => res.json(makePlaylist())); // ✅ SINGLE PlayListType

app.get('/api/spotify/album/freshAlbums', (req, res) => {
  const result = Array.from({ length: 5 }, makePlaylist); // ✅ PlayListType[]
  console.log('freshAlbums returning:', result.length, 'items'); // Debug log
  res.json(result);
});

app.get('/api/spotify/album/:id', (req, res) => res.json(makePlaylist())); // ✅ SINGLE PlayListType

app.get('/api/spotify/playlist_collection/top/10/0', (req, res) => 
  res.json(Array.from({ length: 10 }, makePlaylist)) // ✅ PlayListType[]
);

app.get('/api/spotify/playlist_collection/:id', (req, res) => 
  res.json(makePlaylist()) // ✅ SINGLE PlayListType
);

app.get('/api/spotify/playlist_collection/:userid/liked_plc', (req, res) => 
  res.json(makeHomePageData()) // ✅ SINGLE HomePageDataType
);

app.get('/api/spotify/playlist_collection/getTopFollowingPlayListCollections', (req, res) => 
  res.json(makeHomePageData())
);

app.get('/api/spotify/playlist_collection/getEditorsPlayList/:limit', (req, res) => 
  res.json(makeHomePageData())
);

app.get('/api/spotify/playlist_collection/getLatestPlayListCollections', (req, res) => 
  res.json(makeHomePageData())
);

app.get('/api/spotify/playlist_collection/:userid/listened_plc/last_played', (req, res) => 
  res.json(makeHomePageData())
);

app.put('/api/spotify/playlist_collection/:userid/liked_plc/:stationid', (req, res) => success(res));
app.put('/api/spotify/playlist_collection/:userid/listened_plc/:stationid/:duration', (req, res) => success(res));

// TRACKS
app.get('/api/spotify/tracks/:userid/queue', (req, res) => 
  res.json(Array.from({ length: 5 }, makeSong)) // ✅ SongType[]
);

app.get('/api/spotify/tracks/top_scoring_songs/:userid/:limit', (req, res) => 
  res.json(Array.from({ length: parseInt(req.params.limit)||5 }, makeSong))
);

app.get('/api/spotify/tracks/top_scoring_songs/:limit', (req, res) => 
  res.json(Array.from({ length: parseInt(req.params.limit)||5 }, makeSong))
);

app.get('/api/spotify/tracks/:userid/next_item_in_queue', (req, res) => 
  res.json({ song: makeSong(), updated_queue: Array.from({ length: 3 }, makeSong) })
);

app.get('/api/spotify/tracks/:userid/prev_item_in_queue', (req, res) => 
  res.json({ song: makeSong(), updated_queue: Array.from({ length: 3 }, makeSong) })
);

app.get('/api/spotify/tracks/:id', (req, res) => res.json(makeSong())); // ✅ SINGLE SongType

app.delete('/api/spotify/tracks/:id', (req, res) => res.send('deleted'));

app.put('/api/spotify/tracks/:id/:trackid', (req, res) => success(res));
app.post('/api/spotify/tracks/:id/add-multiple', (req, res) => success(res, 201));
app.delete('/api/spotify/tracks/:id/:trackid', (req, res) => success(res));

app.put('/api/spotify/users/:id/likedislike/song/:trackid/:likedislike', (req, res) => success(res));
app.put('/api/spotify/users/:id/likedislike/radio/:trackid/:likedislike', (req, res) => success(res));
app.put('/api/spotify/users/:id/likedislike/podcast/:trackid/:likedislike', (req, res) => success(res));

app.post('/api/spotify/tracks/:songid/addS3Link/:s3link', (req, res) => success(res));

app.post('/api/spotify/tracks/findmultiplebynames/:userid', (req, res) => 
  res.status(201).json([{ query: 'test', results: Array.from({ length: 2 }, makeSong) }])
);

app.post('/api/spotify/tracks/findmultiplebyids/:userid', (req, res) => 
  res.status(201).json([{ query: 'test', results: Array.from({ length: 2 }, makeSong) }])
);

app.post('/api/spotify/tracks/findbyname/:userid', (req, res) => 
  res.status(201).json(Array.from({ length: 2 }, makeSong))
);

app.post('/api/spotify/stream/uploadAll/:id', (req, res) => success(res, 201));
app.post('/api/spotify/stream/', (req, res) => success(res, 201));

app.get('/api/spotify/stream/all_available_songs/:skip/:limit', (req, res) => 
  res.json({ page: 1, results: Array.from({ length: parseInt(req.params.limit)||10 }, makeSong) })
);

app.get('/api/spotify/stream/all_songs/:page', (req, res) => 
  res.json(Array.from({ length: 10 }, makeSong))
);

app.post('/api/spotify/stream/markbadlink/:id/:bad', (req, res) => success(res, 201));
app.get('/api/spotify/stream/allbadlink', (req, res) => 
  res.json([{ id: uuid(), bad: true }])
);

app.post('/api/spotify/tracks/requestTrackAddition/:userid/:songid', (req, res) => 
  res.json({ id: req.params.songid, name: 'Track', album: 'Album', artist: 'Artist' })
);

// ARTISTS
app.get('/api/spotify/artist/songs/:id/:page', (req, res) => 
  res.json({ page: parseInt(req.params.page), results: Array.from({ length: 5 }, makeSong) })
);

app.get('/api/spotify/artist/top', (req, res) => 
  res.json(Array.from({ length: 5 }, makeArtist)) // ✅ ArtistType[]
);

app.get('/api/spotify/artist/:id/playlists/:limit/:skip', (req, res) => 
  res.json(Array.from({ length: parseInt(req.params.limit)||5 }, makePlaylist)) // ✅ PlayListType[]
);

app.get('/api/spotify/artist/:id/playlists_collection/:limit/:skip', (req, res) => 
  res.json(Array.from({ length: parseInt(req.params.limit)||5 }, makePlaylist))
);

app.get('/api/spotify/artist/:id', (req, res) => res.json(makeArtist())); // ✅ SINGLE ArtistType

app.get('/api/spotify/artist/all/:page', (req, res) => 
  res.json({
    page: parseInt(req.params.page),
    results: Array.from({ length: 5 }, () => ({
      image: img('Artist'),
      id: uuid(),
      title: `Artist ${uuid().slice(0,4)}`
    }))
  })
);

app.get('/api/spotify/album/all/:page', (req, res) => 
  res.json({
    page: parseInt(req.params.page),
    results: Array.from({ length: 5 }, () => ({
      image: img('Album'),
      id: uuid(),
      title: `Album ${uuid().slice(0,4)}`
    }))
  })
);

// PLAYLIST COLLECTIONS (Pagination)
app.get('/api/spotify/playlist_collection/all/:page', (req, res) => 
  res.json({
    page: parseInt(req.params.page),
    results: Array.from({ length: 5 }, () => ({
      image: img('Collection'),
      id: uuid(),
      title: `Collection ${uuid().slice(0,4)}`
    }))
  })
);

app.get('/api/spotify/playlist_collection/browseByGenre/:page', (req, res) => 
  res.json({
    page: parseInt(req.params.page),
    results: Array.from({ length: 5 }, () => ({
      image: img('Genre'),
      id: uuid(),
      title: `Genre ${uuid().slice(0,4)}`
    }))
  })
);

app.get('/api/spotify/playlist_collection/browseTop10PlaylistCollectionByGenre/:userid/:page', (req, res) => 
  res.json({
    page: parseInt(req.params.page),
    results: Array.from({ length: 3 }, makeHomePageData)
  })
);

// SEARCH
app.get('/api/spotify/search/:userid/plc_genres', (req, res) => 
  res.json([{ value: 'Pop', count: 10 }, { value: 'Rock', count: 5 }])
);

app.get('/api/spotify/search/:test/:query/:type/:page', (req, res) => 
  res.json({
    total: 100,
    took: 10,
    cards: Array.from({ length: 5 }, () => ({
      play_url: 'https://example.com/play',
      id: uuid(),
      image: img('Search'),
      name: 'Result',
      artist: 'Artist',
      type: req.params.type || 'songs'
    })),
    type: req.params.type || 'songs'
  })
);

app.get('/api/spotify/search/:userid/:query/:empty/:page', (req, res) => 
  res.json({
    total: 100,
    took: 10,
    cards: Array.from({ length: 5 }, () => ({
      play_url: 'https://example.com/play',
      id: uuid(),
      image: img('Search'),
      name: 'Result',
      artist: 'Artist',
      type: 'songs'
    })),
    type: 'songs'
  })
);

app.get('/api/spotify/search/:id/song_genres', (req, res) => 
  res.json([{ value: 'Pop', count: 100 }, { value: 'Rock', count: 50 }].filter(x => x.value.length > 3).sort((a,b) => b.count - a.count))
);

// USER PLAYLISTS
app.post('/api/spotify/playlist/:userid/create-new-playlist/:name', (req, res) => 
  res.status(201).json({
    id: uuid(),
    name: req.params.name,
    image: img('NewPL'),
    tracks: [],
    user_id: req.params.userid,
    followers: [],
    created_at: new Date().toISOString(),
    updated_at: new Date().toISOString()
  })
);

app.put('/api/spotify/playlist/:userid/:songid', (req, res) => res.json(makePlaylist()));

app.get('/api/spotify/playlist/:userid/playlists', (req, res) => 
  res.json(Array.from({ length: 5 }, () => ({
    id: uuid(),
    name: `Playlist ${uuid().slice(0,4)}`,
    image: img('UserPL'),
    tracks: [uuid(), uuid()],
    user_id: req.params.userid,
    followers: [uuid()],
    created_at: new Date().toISOString(),
    updated_at: new Date().toISOString()
  }))) // ✅ UserPlayListType[]
);

app.get('/api/spotify/playlist/:user_id/fav', (req, res) => res.json(makePlaylist())); // ✅ SINGLE

app.delete('/api/spotify/playlist/:userid/:playlistid', (req, res) => success(res));

app.put('/api/spotify/users/:id/:playlistid/follow-playlist', (req, res) => success(res));
app.put('/api/spotify/users/:id/:playlistid/unfollow-playlist', (req, res) => success(res));

app.post('/api/spotify/playlist/uploadImage/', (req, res) => 
  res.status(201).json({ url: 'https://s3.example.com/image.jpg' })
);

// RADIO
app.get('/api/spotify/radio/:userid/liked_stations', (req, res) => 
  res.json(Array.from({ length: 5 }, makeRadio)) // ✅ RadioStation[]
);

app.get('/api/spotify/radio/:countryCode/getTop10StationsByGenres/:genre', (req, res) => 
  res.json(Array.from({ length: 10 }, makeRadio))
);

app.get('/api/spotify/radio/:countryCode/browseStationsByCountryAndGenre/:page', (req, res) => 
  res.json({ page: parseInt(req.params.page), results: Array.from({ length: 5 }, makeRadio) })
);

app.get('/api/spotify/radio/:countryCode/browseStationsByCountry/:page', (req, res) => 
  res.json({ page: parseInt(req.params.page), results: Array.from({ length: 5 }, makeRadio) })
);

app.get('/api/spotify/radio/:countryCode/trendingStations', (req, res) => 
  res.json(Array.from({ length: 10 }, makeRadio))
);

app.get('/api/spotify/radio/:userid/last_played_stations', (req, res) => 
  res.json(Array.from({ length: 5 }, makeRadio))
);

app.put('/api/spotify/radio/:userid/listened_station/:stationid/:duration', (req, res) => success(res));

app.get('/api/spotify/radio/all_countries', (req, res) => 
  res.json([{ name: 'United States', count: 100, code: 'US' }, { name: 'United Kingdom', count: 50, code: 'UK' }])
);

app.get('/api/spotify/radio/all_genres/:countryCode', (req, res) => 
  res.json([{ value: 'News', count: 10 }, { value: 'Music', count: 20 }])
);

app.get('/api/spotify/radio/:id', (req, res) => res.json(makeRadio())); // ✅ SINGLE RadioStationType

// PODCASTS
app.get('/api/spotify/podcast/all_genres', (req, res) => 
  res.json([{ value: 'Tech', count: 10 }, { value: 'Comedy', count: 20 }])
);

app.get('/api/spotify/podcast/:userid/liked_podcasts', (req, res) => 
  res.json(Array.from({ length: 5 }, makePodcastCard)) // ✅ PodcastCardDto[]
);

app.get('/api/spotify/podcast/getTop10PodcastsByGenres/:genre', (req, res) => 
  res.json(Array.from({ length: 10 }, makePodcastCard))
);

app.get('/api/spotify/podcast/browsePodcastsByGenre/:genre/:page', (req, res) => 
  res.json({
    page: parseInt(req.params.page),
    results: Array.from({ length: 3 }, () => ({
      cards: Array.from({ length: 5 }, makePodcastCard),
      label: 'Genre Section'
    }))
  })
);

app.get('/api/spotify/podcast/browse/:page', (req, res) => 
  res.json({
    page: parseInt(req.params.page),
    results: Array.from({ length: 3 }, () => ({
      cards: Array.from({ length: 5 }, makePodcastCard),
      label: 'Browse Section'
    }))
  })
);

app.put('/api/spotify/podcast/:userid/liked_podcast/:podcastid/:episodeid', (req, res) => success(res));

app.put('/api/spotify/podcast/:userid/listened_station/:podcastid/:episodeid/:duration', (req, res) => 
  res.json(Array.from({ length: 3 }, makePodcastCard))
);

app.get('/api/spotify/podcast/:id/episodes/:page', (req, res) => 
  res.json({ page: parseInt(req.params.page), results: Array.from({ length: 5 }, makePodcastEpisode) })
);

app.get('/api/spotify/podcast/:id', (req, res) => res.json(makePodcastCard())); // ✅ SINGLE PodcastCardDto

app.get('/api/spotify/podcast/:id/topPodcastsByUserActivity', (req, res) => 
  res.json(Array.from({ length: 5 }, makePodcastCard))
);

app.post('/api/spotify/podcast/requestPodcastEpisodesPopulation/:userid/:songid', (req, res) => success(res, 201));

// ADDITION SERVICE
app.get('/findAndSendSongsToSavedMessage/:pid', (req, res) => success(res));

// CATCH-ALL
app.use((req, res) => {
  console.log(`❌ 404: ${req.method} ${req.path}`);
  res.status(404).json({ error: 'Endpoint not mocked' });
});

// START
app.listen(PORT, () => {
  console.log('\n✅ Mock server running: http://localhost:' + PORT);
  console.log('📍 Update your index.ts:');
  console.log('   content_base_url = "http://localhost:9000/api/spotify"');
  console.log('   addition_base_url = "http://localhost:9000"');
});