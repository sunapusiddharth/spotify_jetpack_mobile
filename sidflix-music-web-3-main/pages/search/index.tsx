import { Box, Chip, Tab, Tabs } from "@mui/material";
import React, { useContext, useEffect, useState, useCallback } from "react"
import { searchAll, requestSpotifysearch } from "../../api";
import { AppContext, AppContextType } from "../_app";
import AllResults from "../../components/search/AllResults";
import Albums from "../../components/search/Albums";
import { SearchPageResType, SearchTypeEnum } from "../../types/Song.type";

import { SearchBar } from "../../components/search/SearchBar";
import { AlbumsSearch } from "../../components/search/AlbumsSearch";
import { Artists } from "../../components/search/Artists";

interface PageLinkProps { }

interface TabPanelProps {
    children?: React.ReactNode;
    index: number;
    value: number;
}

function TabPanel(props: TabPanelProps) {
    const { children, value, index, ...other } = props;

    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`simple-tabpanel-${index}`}
            aria-labelledby={`simple-tab-${index}`}
            {...other}
        >
            {value === index && <>{children}</>}
        </div>
    );
}

function a11yProps(index: number) {
    return {
        id: `simple-tab-${index}`,
        'aria-controls': `simple-tabpanel-${index}`,
    };
}
const SearchComp = () => {
    const [movies, setMovies] = useState<SearchPageResType>()
    const [loading, setLoading] = useState<boolean>(false)
    const { searchTerm, user } = useContext(AppContext) as AppContextType
    const [value, setValue] = React.useState(0);

    const handleChange = (event: React.SyntheticEvent, newValue: number) => {
        setValue(newValue);
    };
    const fetchData = useCallback(async () => {
        setLoading(true)
        return await searchAll(searchTerm, undefined, 1).then(response => {
            setMovies(response)
            setLoading(false)
        })
        setLoading(false)
    }, [searchTerm]);

    useEffect(() => {
        fetchData()
        return () => setMovies(undefined)
    }, [fetchData])



    const requestSpotifySearching = async () => {
        if (!user) return
        await requestSpotifysearch(user.id, searchTerm, 1)
    }

    // if (!movies || !movies.length || !searchTerm) return <div >
    //     <Typography>We couldn't find any song related to your search</Typography>
    //     <Button variant='contained' color='secondary' onClick={requestSpotifySearching}>Search In Partner</Button>
    // </div>
    // if (loading) return <div className="loading-content"><div className="loading-circle"></div><span className="loading-name">LOADING...</span></div>
    return (
        <Box sx={{ width: '100%', paddingX: 1 }} >
            <SearchBar />
            <Tabs sx={{ marginTop: 1 }} value={value} onChange={handleChange} aria-label="search tabs">
                <Tab label={<Chip label="All" sx={{ textTransform: 'capitalize' }} />} {...a11yProps(0)} />
                <Tab label={<Chip label="Songs" sx={{ textTransform: 'capitalize' }} />}  {...a11yProps(1)} />
                <Tab label={<Chip label="Albums" sx={{ textTransform: 'capitalize' }} />} {...a11yProps(2)} />
                <Tab label={<Chip label="Artist" sx={{ textTransform: 'capitalize' }} />} {...a11yProps(3)} />
                <Tab label={<Chip label="Radio" sx={{ textTransform: 'capitalize' }} />} {...a11yProps(4)} />
                <Tab label={<Chip label="Podcast" sx={{ textTransform: 'capitalize' }} />} {...a11yProps(5)} />
            </Tabs>
            <TabPanel value={value} index={0}>
                {searchTerm && movies?.cards?.length ? <AllResults movies={movies} /> : <></>}
            </TabPanel>
            <TabPanel value={value} index={1}>
                <Albums type={SearchTypeEnum.songs} />
            </TabPanel>
            <TabPanel value={value} index={2}>
                {searchTerm ? <Albums type={SearchTypeEnum.album} /> : <AlbumsSearch />}
            </TabPanel>
            <TabPanel value={value} index={3}>
                {searchTerm ? <Albums type={SearchTypeEnum.artist} /> : <Artists />}
            </TabPanel>
            <TabPanel value={value} index={4}>
                <Albums type={SearchTypeEnum.radio} />
            </TabPanel>
            <TabPanel value={value} index={5}>
                <Albums type={SearchTypeEnum.podcast} />
            </TabPanel>
        </Box>
    )
}

function moviePropsAreEqual(
    prevProps: PageLinkProps,
    nextProps: PageLinkProps
) {
    // //console.log("Props validation",prevProps === nextProps,prevProps ,nextProps)
    return JSON.stringify(prevProps) === JSON.stringify(nextProps);
}
export const SearchPage = React.memo(
    SearchComp,
    moviePropsAreEqual
);
export default SearchPage;


// export const getServerSideProps = hasNavigationCSR(async (context: any) => {
//     const session = await getSession(context)
//     const queryClient = new QueryClient();
//     //@ts-ignore
//     const userid = session?.user?.id
//     if (!queryClient.getQueryData(['homepage'])) {
//         await queryClient.prefetchInfiniteQuery(["homepage"], async () => fetchHomePageData(userid, 1))
//     }
//     await Promise.all([
//         queryClient.prefetchQuery(['browseStationsByCountry', 'IN', 1], async () => await browseStationsByCountry('IN', 1)),
//         queryClient.prefetchQuery(["editor_playlist"], async () => getEditorsPlayList(10)),
//         queryClient.prefetchQuery(["top_artists"], async () => fetchTopArtists()),
//         queryClient.prefetchQuery(["topScoringTracksForUser"], async () => getTopScoringSongsForuser(userid, 10)),
//         queryClient.prefetchQuery(["top_podcasts"], async () => browsePodcasts(1)),

//     ])
//     return { props: { dehydratedState: JSON.parse(JSON.stringify(dehydrate(queryClient))) } };
// })




const ArtistsRender = () => {

}

