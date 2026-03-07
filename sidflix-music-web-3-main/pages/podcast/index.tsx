import * as React from "react";
import { dehydrate, QueryClient, useInfiniteQuery, useQuery } from "react-query";
import NoSsr from "@mui/material/NoSsr";
import { Box, Button, Grid, Typography } from "@mui/material";
import { browsePodcasts, getAllPodcastGenres, getUserLikedPodcasts, topPodcastsByUserActivity } from "../../api";
import { AppContext, AppContextType } from "../_app";
import { PodcastCarousel } from "../../components/podcast/PodcastCarousel";
// import Grid from '@mui/material/Unstable_Grid2'; // Grid version 2
import { PodcastCard } from "../../components/podcast/PodcastCard";
import { getSession, useSession } from "next-auth/react";
import router from "next/router";
import Link from "next/link";

interface PageProps {

}

const PodcastsLandingPage: React.FC<PageProps> = ({
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
    const { data: podcast_genres } = useQuery(["podcast_genres"], async () => await getAllPodcastGenres());
    const { data: top_podcast_by_user_activity } = useQuery(["top_podcast_by_user_activity"], async () => await topPodcastsByUserActivity(user?.id!));
    const { data: user_liked_podcasts } = useQuery(["getUserLikedPodcasts"], async () => await getUserLikedPodcasts(user?.id!));
    const { isSuccess, isLoading, isError, data: top10PodcastsData, error, hasNextPage, fetchNextPage, isFetchingNextPage } = useInfiniteQuery(
        ["top_podcasts"],
        async ({ pageParam = 1 }) => await browsePodcasts(pageParam), {
        getNextPageParam: (lastPage, pages) => {
            return lastPage.page + 1
        },
        keepPreviousData: true,
        refetchOnMount: false,
        refetchOnWindowFocus: false,
        staleTime: 9000,
        cacheTime: 90000,
    });
    return < >
        <NoSsr>
            <Box sx={{
                overflowY: 'scroll',

            }}>
                <PodcastCarousel data={{ label: 'You Liked These Podcasts', cards: user_liked_podcasts || [] }} genreid={''} />
                <PodcastCarousel data={{ label: 'You May Like', cards: top_podcast_by_user_activity || [] }} genreid={''} />
                <Grid container className='podcast_gehnres' spacing={1}>
                    {podcast_genres?.map(genre => <Grid key={'genre-' + genre.value} item> <Link href={'/podcast/genre/' + genre.value}>
                        <Button variant="outlined" size="small" color="info" sx={{ maxWidth: 200, fontSize: 'x-small' }}>{genre.value.toLocaleLowerCase()} ({genre.count})</Button>
                    </Link></Grid>)}
                </Grid>
                <Typography variant="subtitle2" mt={2}>Top Podcasts</Typography>
                <div  style={{display:'flex',columnGap:5,flexWrap:"wrap"}}>
                    {isSuccess && top10PodcastsData?.pages && <>
                        {top10PodcastsData.pages?.map((page, i) => page.results?.map((x, j) =>
                            // <Grid key={'top-podcasts' + j + x.uuid} item>
                                <PodcastCard station={x} key={'top-podcasts' + j + x.uuid}/>
                            // </Grid>
                        ))}
                    </>
                    }
                </div>
                {hasNextPage && <div className='btn-container' style={{ textAlign: 'center', marginTop: '2%' }}>
                    <Button variant='contained' onClick={() => fetchNextPage()} size='small' sx={{ color: 'white', background: 'purple', fontWeight: 'bolder' }}>Load More</Button>
                </div>}
                <div>{isLoading && !isFetchingNextPage ? 'Fetching...' : null}</div>
            </Box>
        </NoSsr>
    </>
};

export default PodcastsLandingPage;

export async function getServerSideProps(context: any) {
    let artistId = ''
    const session = await getSession(context)
    //@ts-ignore
    const userid = session?.user?.id
    if (context.query.id) artistId = context.query.id;
    const queryClient = new QueryClient();
    if (!queryClient.getQueryData(['top_podcasts'])) {
        await queryClient.prefetchInfiniteQuery(["top_podcasts"], async () => browsePodcasts(1))
        await Promise.all([
            queryClient.prefetchQuery(["podcast_genres", artistId], async () => getAllPodcastGenres()),
            queryClient.prefetchQuery(["top_podcast_by_user_activity", artistId], async () => topPodcastsByUserActivity(userid)),
            queryClient.prefetchQuery(["getUserLikedPodcasts", artistId], async () => getUserLikedPodcasts(userid)),
        ])
    
    }
   
    return { props: { dehydratedState: JSON.parse(JSON.stringify(dehydrate(queryClient))) } };
}


