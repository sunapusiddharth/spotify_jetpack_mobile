import * as React from "react";
import NoSsr from "@mui/material/NoSsr";
import router from "next/router";
import { useSession } from "next-auth/react";
import { Box, Button, Link, Typography } from "@mui/material";
import { useInfiniteQuery, useQuery } from "react-query";
import { getLatestPlayListCollections, getTopFollowingPlayListCollections, getLastPlc, getuserLikedPlcs, browseTop10PlaylistCollectionByGenre, fetchPlcGenres } from "../../api";
import { CardsListing } from "../../components/home/Carousel";
import { AppContext, AppContextType } from "../_app";
interface PageProps {
}

const RadioComp: React.FC<PageProps> = ({
}: PageProps) => {
    const { data: session } = useSession()
    React.useEffect(() => {
        if (!session) {
            router.push("/auth/signin");
        }
    }, []);

    const { user } = React.useContext(
        AppContext
    ) as AppContextType;
    const { isSuccess,
        isLoading,
        isError,
        data,
        error,
        hasNextPage,
        fetchNextPage,
        isFetchingNextPage
    } = useInfiniteQuery(
        ["plc_genres_browse"],
        async ({ pageParam = 1 }) => await browseTop10PlaylistCollectionByGenre(user?.id, pageParam), {
        getNextPageParam: (lastPage, pages) => {
            return lastPage.page + 1
        },
        keepPreviousData: true,
        refetchOnMount: false,
        refetchOnWindowFocus: false
    });

    const { data: latestPlayListCollections, isLoading: loading1 } = useQuery(['latestPlayListCollections'], async () => await getLatestPlayListCollections());
    const { data: topFollowingPlayListCollections, isLoading: loading2 } = useQuery(['topFollowingPlayListCollections'], async () => await getTopFollowingPlayListCollections());
    const { data: lastPlc, isLoading: loading4 } = useQuery(['lastPlc', user?.id], async () => await getLastPlc(user?.id));
    const { data: userLikedPlcs, isLoading: loading6 } = useQuery(['userLikedPlcs', user?.id], async () => await getuserLikedPlcs(user?.id));
    // const { data: plcGenres } = useQuery(["plcGenres"], async () => await fetchPlcGenres(user?.id));
    return (
        <NoSsr>
            <Box sx={{
                overflowY: 'scroll',
                overflowX: 'hidden',
                marginTop: '2%'
            }}>
                {/* <Box className='plc_genres' >
                    {plcGenres?.aggregations?.states?.tags_here?.buckets?.map(genre => <Link href={'/playlist_collection/genre/' + genre.key} key={'plcgenre-' + genre.key}><Box className="plc_genre_card">
                        <Typography variant='h6' sx={{ height: '114px !important', lineHeight: ' 114px !important' }}>{genre.key}({genre.doc_count})</Typography>
                    </Box></Link>)}
                </Box> */}
                <Box sx={{ paddingTop: 3 }} >{lastPlc?.cards?.length && <CardsListing data={lastPlc} />}</Box>
                <Box sx={{ paddingTop: 3 }} >{userLikedPlcs?.cards?.length && <CardsListing data={userLikedPlcs} />}</Box>
                <Box sx={{ paddingTop: 3 }} >{latestPlayListCollections?.cards?.length && <CardsListing data={latestPlayListCollections} />}</Box>
                <Box sx={{ paddingTop: 3 }} >{topFollowingPlayListCollections?.cards?.length && <CardsListing data={topFollowingPlayListCollections} />}</Box>
                {isSuccess && data?.pages && <>
                    {data.pages?.map((page, i) => (
                        <Box key={'plc-carousels-genre-browse-Pge' + page + i}>
                            {page.results?.map((x, j) =>
                                <Box sx={{ paddingTop: 3 }} key={j}>
                                    <CardsListing data={x} key={'plc-carousels-genre-browsecardlisitng-' + i} />
                                </Box>
                            )}
                        </Box>
                    ))}
                    <div className='btn-container' style={{ textAlign: 'center', marginTop: '2%' }}>
                        <Button variant='contained' onClick={() => fetchNextPage()} size='small' sx={{ color: 'white', background: 'purple', fontWeight: 'bolder' }}>Load More</Button>
                    </div>
                    <div>{isLoading && !isFetchingNextPage ? 'Fetching...' : null}</div>
                </>
                }
            </Box>
        </NoSsr>
    );
};

export const QueuePage = React.memo(
    RadioComp
    // propsAreEqual
);
export default QueuePage;