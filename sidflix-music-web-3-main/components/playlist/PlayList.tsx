import * as React from "react";
import Typography from '@mui/material/Typography';
import { Box, Button, Chip, Grid, Tooltip } from '@mui/material'
import { AppContext, AppContextType } from "../../pages/_app";
import { useQueryClient } from "react-query";
import { addPlayListToQueue, requestTrackAddition } from "../../api";
import { millisToMinutesAndSeconds } from "../../utils";
import Link from "next/link";
import { PlayCircleFilledRounded } from "@mui/icons-material";
import PlayListSongs from "./PlayListSongs";
import Image from "next/image";
import { PlayListType } from "../../types/PlayListType";

interface CompProps {
    fav: boolean
    data: PlayListType | undefined
}
function PlayListComp(props: CompProps) {

    const { fav, data } = props
    const { user, setCurrentSong } = React.useContext(
        AppContext
    ) as AppContextType;
    // const { data } = useQuery(["playlist", playlist], async () => { if (is_user_plc) { return await getFav(user?.id!) } else { return await fetchPlaylistData(playlist) } },);
    const queryClient = useQueryClient()
    const playAllSongs = async () => {
        if (user && user.id && data) {
            await addPlayListToQueue(user.id, data.songs.map(x => x.id))
            await queryClient.refetchQueries(['queue', user.id])
            setCurrentSong(data.songs[0])
            await queryClient.refetchQueries(['user', user.id])
        } else {
            console.error('No Error is found')
        }
    }

    async function handleAddplcTrack() {
        if (!data?.songs) {
            console.error(`No songs in playlist`)
            return
        }
        const songsWithoutS3link = data?.songs?.filter(x => !x.s3link)
        if (!songsWithoutS3link?.length) {
            console.error(`All songs already downloaded`)
            return
        }
        for (const song of data?.songs) {
            try {
                const res = await requestTrackAddition(user?.id!, song.id)
                if (res == 'success') {
                    alert(`Your song ${song.name} is now available`)
                }
                if (res == 'failed') {
                    alert(`Song ${song.name} was requested , there was a problem in downloading ! we will upload it by end of day.`)
                }
                if (res == 'failed2') {
                    alert(`Song ${song.name} was not requested successfully`)
                }
            } catch (error) {
                alert(`Something went wrong  for song ${song.name} please try later.`)
            }
        }
    }

    return <Box mt={4}>
        {!fav ? <Grid container alignItems='center' columnGap={4}>
            <Grid item width={450} height={300} sx={{ background: 'black' }}>
                <Image alt="" src={data?.image && data.image !== 'no-cover.jpg' ? data?.image : '/cover.jpg'} width={450} height={300} />
            </Grid>
            <Grid item>
                <Typography fontSize="large">{data?.title}</Typography>
                <Typography fontSize="medium">{data?.artists.slice(0, 4).map(x => <Link href={`artist/${x.id}`} key={'artist' + x.id}>{x.name + '  '}</Link>)} {data?.artists?.length! > 4 && <span>and more</span>}</Typography>
                <Typography fontSize="medium"><Chip label={data?.songs.length + ' songs'} /> <Chip label={millisToMinutesAndSeconds(data?.songs.reduce((acc, x) => acc += x.duration, 0) || 0) + ' mins'} />   </Typography>
                <Grid container columnGap={4} sx={{ marginTop: 5, padding: 1 }} alignItems='center'>
                    <Grid item display="flex" flexDirection={'column'}>
                        <Tooltip title="play all">
                            <Box sx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', columnGap: 1 }}>
                                <PlayCircleFilledRounded fontSize="large" onClick={playAllSongs} sx={{ cursor: 'pointer' }} className="play-all-songs" />
                                <Typography>Play All</Typography>
                            </Box>
                        </Tooltip>
                    </Grid>
                    <Grid item>
                        <Tooltip title="Request Tracks in Playlit">
                            <Button onClick={handleAddplcTrack} disabled={!data?.songs.length}>Add All</Button>
                        </Tooltip>
                    </Grid>
                </Grid>
            </Grid>
        </Grid> : <Typography fontSize="large" sx={{ margin: 3 }} textTransform='uppercase'>{data?.title}</Typography>}

        <PlayListSongs data={data?.songs || []} />
    </Box>
}

function moviePropsAreEqual(
    prevProps: CompProps,
    nextProps: CompProps
) {
    ////console.log("Sidhu",prevProps,nextProps);
    return prevProps === nextProps;
}

export const PlayList = React.memo(
    PlayListComp,
    moviePropsAreEqual
);
