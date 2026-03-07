import * as React from "react";
import { dehydrate, QueryClient, useInfiniteQuery, useQuery, useQueryClient } from "react-query";
import NoSsr from "@mui/material/NoSsr";
import { Box, Button, ButtonGroup, Divider, Snackbar, Tooltip, Typography } from "@mui/material";
import { getPodcastDetails, getPodcastEpisoes, requestPodcastEpisodesPopulation, userListenedPodcasts } from "../../api";
import { AppContext, AppContextType } from "../_app";
import { useRouter } from "next/router";
import { PodcastEpisodeDto } from "../../types/PodcastEpisode.dto";
import { millisToMinutesAndSeconds, removeTags } from "../../utils";
import { useSession } from "next-auth/react";
import { Info, } from "@mui/icons-material";

interface PageProps {

}

const PodcastPage: React.FC<PageProps> = ({
}: PageProps) => {
    const [openSnack, setopenSnack] = React.useState<boolean>(false)
    const [message, setMessage] = React.useState<string>('')
    const { data: session } = useSession()
    React.useEffect(() => {
        if (!session) {
            router.push("/auth/signin");
        }
    }, []);
    const { user, setPodcast } = React.useContext(
        AppContext
    ) as AppContextType;
    const router = useRouter();
    const queryClient = useQueryClient()
    const [selectedSong, setSelectedSong] = React.useState<PodcastEpisodeDto>();
    const [addedPodcast, setaddedPodcast] = React.useState<string | undefined>();
    // const saveToLikedSongs = useMutation(({ songid, likedislike }: { songid: string, likedislike: boolean }) => liekdislikesong(user?.id || '1', likedislike, songid), {
    //     onSuccess: (data) => {
    //         queryClient.setQueryData(['user', { id: user?.id }], data)
    //         setUser(data)
    //         console.log("OnSuccess", user?.id, data)
    //     }
    // })

    async function playEpisode(song: PodcastEpisodeDto | undefined) {
        if (song && podcast) {
            setPodcast({ episode: song, podcast })
            setaddedPodcast(song.uuid)
            await userListenedPodcasts(user?.id!, podcast?.uuid!, 0, song.uuid)
        }

    }
    function addToWatchLater() {
        if (selectedSong) {
            //make api call to save this data
            // setPodcast(selectedSong)
        }
    }

    const addToQueue = () => { }
    // const [addToPlayListSong, setaddToPlayListSong] = React.useState<SongType>();
    const [open, setOpen] = React.useState<boolean>(false);
    // const hadleAddtoplaylist = (song: SongType) => {
    //     setaddToPlayListSong(song)
    //     setOpen(true)
    // }

    function setHoverData(data: PodcastEpisodeDto) {
        setSelectedSong(data);
    }
    let podcastid: string;
    if (router.query.id)
        podcastid = router.query.id as string;
    const { data: podcast } = useQuery(["podcast_details", podcastid!], async () => await getPodcastDetails(podcastid));
    const { isSuccess, isLoading, isError, data, error, hasNextPage, fetchNextPage, isFetchingNextPage } = useInfiniteQuery(
        ["podcastEpisodes", podcastid!],
        async ({ pageParam = 1 }) => await getPodcastEpisoes(podcastid, pageParam), {
        getNextPageParam: (lastPage, pages) => {
            return lastPage.page + 1
        }
    });


    async function requestAdditionOfPodcast() {
        try {
            await requestPodcastEpisodesPopulation(user?.id!, podcastid)
            await queryClient.invalidateQueries(["podcastEpisodes", podcastid!])
            setMessage('Podcast Episodes requested,We will try it as soon as possible')
            setopenSnack(true)
        } catch (error) {
            setMessage(`Podcast Episodes requested,Error ${error}`)
            setopenSnack(true)
        }

    }

    return < >
        <NoSsr>
            <Box p={1} sx={{
                overflowY: 'scroll',
                overflowX: 'hidden'
            }} >
                <Box alignItems='center' columnGap={3} flexWrap='nowrap' className='podcast_single' sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row', lg: 'row' } }}>
                    <Box sx={{ background: 'black' }} >
                        <img src={podcast?.image_url || '/cover.jpg'} width={200} height={200} />
                    </Box>
                    <Box sx={{ display: 'flex', rowGap: '8px', flexDirection: 'column' }} >
                        <Typography fontSize="large">{podcast?.title}</Typography>
                        <Box display="flex" flexDirection={"column"}>
                            <Typography color="lightgrey" fontSize="small">{podcast?.genres}</Typography>
                            <Typography color="lightgrey" fontSize="small">{podcast?.episode_count} episodes</Typography>
                            <Typography color="lightgrey" fontSize="small">By {podcast?.itunes_author}</Typography>
                        </Box>
                        <Box sx={{ columnGap: '1%' }} display="flex">
                            <Button size="small" fullWidth={false} variant="contained" onClick={() => playEpisode(data?.pages[0]?.results[0])} color="secondary" >Play Latest</Button>
                            <Button size="small" fullWidth={false} variant="contained" onClick={requestAdditionOfPodcast} color="secondary">Request</Button>
                        </Box>
                        <Typography fontSize="small" color="lightgrey">{removeTags(podcast?.description?.replace(/(href=["'])([A-Za-z-\\\/:.]*)(["'])/ig, "")) || ''}</Typography>
                    </Box>
                </Box>

                <Typography mt={2} variant='subtitle1' className='podcast_single_title'>Episodes</Typography>
                <Box sx={{}}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', rowGap: '10px', maxHeight: 500, overflowY: 'scroll' }}>
                        {data?.pages?.map(page => page.results?.map((song, index) => <HoverableCard key={"podcast-episodes" + song.uuid + index} data={song} podcast={addedPodcast} addToWatchLater={addToWatchLater} playEpisode={playEpisode} />))}
                    </Box>
                    {hasNextPage && <div className='btn-container' style={{ textAlign: 'center', marginTop: '2%' }}>
                        <Button variant='contained' onClick={() => fetchNextPage()} size='small' sx={{ color: 'white', background: 'purple', fontWeight: 'bolder' }}>Load More</Button>
                    </div>}
                    <div>{isLoading && !isFetchingNextPage ? 'Fetching...' : null}</div>
                </Box>
            </Box>
            <Snackbar
                anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
                open={openSnack}
                onClose={() => setopenSnack(false)}
                message={message}
                key='snackbar'
                autoHideDuration={1500}
            />
        </NoSsr>
    </>
};

export default PodcastPage;

export async function getServerSideProps(context: any) {
    let artistId = ''
    if (context.query.id) artistId = context.query.id;
    const queryClient = new QueryClient();
    if (!queryClient.getQueryData(['podcastEpisodes', artistId])) {
        await queryClient.prefetchInfiniteQuery(["podcastEpisodes", artistId], async () => getPodcastEpisoes(artistId, 1))
        await Promise.all([
            queryClient.prefetchQuery(["podcast_details", artistId], async () => getPodcastDetails(artistId)),
        ])
    }


    return { props: { dehydratedState: JSON.parse(JSON.stringify(dehydrate(queryClient))) } };
}



export const HoverableCard = ({ data, podcast, addToWatchLater, playEpisode }: { data: PodcastEpisodeDto, podcast: string | undefined, addToWatchLater: any, playEpisode: any }) => {
    const active = podcast == data.uuid

    return <Box key={'podcast-episode-list' + data.uuid}
        sx={{ display: 'flex', padding: '0.5%', alignItems: 'flex-start', columnGap: '3%' }}
        className={active ? 'selected_podcast' : 'playlist_song'}
        onClick={() => playEpisode(data)}
    >
        <Box flex={0.2}>
            <img src={data.image_url ?? "/sample.jpg"} alt={''}
                width={80} height={80} />
        </Box>
        <Box sx={{ fontSize: 'small', display: 'flex', flexDirection: 'column', flex: 0.4 }}>
            <Typography sx={{ fontSize: 'small', }}
            >{data.name}</Typography>
            <Typography
                sx={{ display: 'inline', color: 'lightgray' }}
                component="span"
                variant="caption"
                // color="text.primary"
                fontSize={'x-small'}
            >
                {data?.date_published ? new Date(data?.date_published).toDateString() : ''}
                <br />{data.duration ? millisToMinutesAndSeconds(data.duration) : ''}
            </Typography>
        </Box>
        <Typography
            flex={1.5}
            sx={{ fontSize: 'smaller', textAlign: 'justify', color: 'lightgray' }}>
            {data.description?.length > 900 ? removeTags(data.description?.replace(/(href=["'])([A-Za-z-\\\/:.]*)(["'])/ig, "").slice(0, 900)) + ' ...' : removeTags(data.description?.replace(/(href=["'])([A-Za-z-\\\/:.]*)(["'])/ig, ""))}
        </Typography>
        <Tooltip title={<div dangerouslySetInnerHTML={{ __html: data.description?.replace(/(href=["'])([A-Za-z-\\\/:.]*)(["'])/ig, "") }} />}>
            <Info />
        </Tooltip>
        {/* <ButtonGroup sx={{ alignItems: 'center' }}>
           
            <IconButton edge="end" aria-label="delete" onClick={() => playEpisode(data)}>
                <PlayArrow />
            </IconButton>

            <IconButton edge="end" aria-label="delete" onClick={addToWatchLater}>
                <PlaylistAdd />
            </IconButton>
        </ButtonGroup> */}

    </Box>
}