Issues:

Issue 1 : Home page Api being called is wrong 

    We need to call 
    export type HomePageInfo = {
    results: HomePageDataType[],
    page: number
    }
    export const fetchHomePageData = async (id: string, page: number): Promise<HomePageInfo> => {
        console.log("home page api called here ",page,"id=",id)

    if (!id) return {
        page: page,
        results: []
    }


    const res = await axios.get<HomePageInfo>(`${content_base_url}/users/${id}/home/${page}`);

    if (res.status === 200) {
        return {
        page: res.data.page,
        results: res.data.results
        };
    }
    throw new Error('Network response not ok');
    };

    we need to call these apis as well in home page and show them on screen
    const { data: topStationsByVotesData, isLoading: loading0 } = useQuery(['browseStationsByCountry', 'US', 1], async () => await browseStationsByCountry('US', 1));

    const { data: editorsPlayListData, isLoading: loading234 } = useQuery(['editor_playlist'], async () => await getEditorsPlayList(10));
    const { data: topArtists, isLoading: loadingTopArtists } = useQuery(['top_artists'], async () => await fetchTopArtists());
    const { data: topScoringTracksForUser, isLoading: loadingtopTracksForuser } = useQuery(['topScoringTracksForUser'], async () => await getTopScoringSongsForuser(user?.id!, 30));
    const { data: top10PodcastsData } = useQuery(["top_podcasts"], async () => await browsePodcasts(1));

    const artCards = topArtists?.length ? uniqueBy<ArtistType>('id', topArtists)?.map(x => ({
        image: x.image,
        id: x.id,
        title: x.title,
        subtitle: '',
        type: 'artists_card',
        path: '/artist/' + x.id,
        song: null
    })) : []
    const artists: HomePageDataType = {
        cardType: 'artist_card',
        label: 'Top Artists',
        path: '/artist',
        //@ts-expect-error
        cards: artCards,
        id: 'top_artists'
    }


Issue 2: AvailableTracksScreen 
    api being fired is wrong we need to fire 
    we need to call this api
    export const getAllAvailableSongs = async (skip: number, limit: number) => {
    const res = await axios.get<AllTracksPageInfo>(`${content_base_url}/stream/all_available_songs/${skip}/${limit}`);
    if (res.status === 200) {
        return res.data;
    }
    throw new Error('Network response not ok');
    };


Issue 3: for PlaylistScreen we need to call 
getPlaylistCollectionById


Issue 4: media controls are not implemented or not working
look at this for reference : https://developer.android.com/media/implement/surfaces/mobile


All done