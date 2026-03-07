import * as React from "react";
import { dehydrate, QueryClient, useInfiniteQuery, useQuery } from "react-query";
import NoSsr from "@mui/material/NoSsr";
import { Box, Button, Grid, Typography } from "@mui/material";
import { useRouter } from "next/router";
import { getTop10PodcastsByGenres, browsePodcastsByGenre } from "../../../api";
import { PodcastCard } from "../../../components/podcast/PodcastCard";
import { PodcastCarousel } from "../../../components/podcast/PodcastCarousel";
import { AppContext, AppContextType } from "../../_app";
import { useSession } from "next-auth/react";

interface PageProps {

}

const PodcastGenrePage: React.FC<PageProps> = ({
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
    const router = useRouter();
    let genre: string = '';
    console.log("A;; router", router.query)
    if (router.query.id)
        genre = router.query.id as string;
    // const { data: top_podcast_by_genre } = useQuery(["top_podcast_by_genre", genre], async () => await getTop10PodcastsByGenres(genre));
    const { isSuccess, isLoading, isError, data, error, hasNextPage, fetchNextPage, isFetchingNextPage } = useInfiniteQuery(
        ["browse_podcasts_by_genre", genre],
        async ({ pageParam = 1 }) => await browsePodcastsByGenre(genre, pageParam), {
        getNextPageParam: (lastPage, pages) => {
            return lastPage.page + 1
        },
        keepPreviousData: true,
        refetchOnMount: false,
        refetchOnWindowFocus: false
    });
    return < >
        <NoSsr>

            <Box mt={5} sx={{
                overflowY: 'scroll',
                overflowX:'hidden'
            }}>
                {/* <PodcastCarousel data={{ label: 'Top Podcasts', cards: top_podcast_by_genre || [] }} genreid={genre} /> */}
                {isError && <Typography sx={{ marginTop: '3% !important' }}> {'Error!' + error}</Typography>}
                {isLoading && <div>Loading...</div>}
                {isSuccess && data?.pages && <>
                    {/* <Typography sx={{ marginTop: '3% !important' }}> Browse All</Typography> */}
                    <Grid container  spacing={{ xs: 2, md: 4 }} columns={{ xs: 2, md: 5 }}>
                        {data.pages?.map((page, i) => (
                            <>
                                {page.results?.map((artist, j) => <Grid item key={'playlists-' + artist.uuid} sx={{ cursor: 'pointer' }}>
                                        <PodcastCard key={`card-listing-${artist.uuid}`} station={artist} />
                                    </Grid>
                                )}
                            </>
                        ))}
                    </Grid>

                    {hasNextPage && <><div className='btn-container' style={{ textAlign: 'center', marginTop: '2%' }}>
                        <Button variant='text' onClick={() => fetchNextPage()} size='small' sx={{ color: 'purple', fontWeight: 'bolder' }}>Load More</Button>
                    </div>
                        <div>{isLoading && !isFetchingNextPage ? 'Fetching...' : null}</div>
                    </>}
                </>}
            </Box>

        </NoSsr>
    </>
};

export default PodcastGenrePage;

export async function getServerSideProps(context: any) {
    let artistId = ''
    if (context.query.id) artistId = context.query.id;
    const queryClient = new QueryClient();
    if (!queryClient.getQueryData(['browse_podcasts_by_genre', artistId])) {
        await queryClient.prefetchInfiniteQuery(["browse_podcasts_by_genre", artistId], async () => browsePodcastsByGenre(artistId, 1))
        await Promise.all([
            queryClient.prefetchQuery(["top_podcast_by_genre", artistId], async () => getTop10PodcastsByGenres(artistId)),
        ])
    }
   

    return { props: { dehydratedState: JSON.parse(JSON.stringify(dehydrate(queryClient))) } };
}

