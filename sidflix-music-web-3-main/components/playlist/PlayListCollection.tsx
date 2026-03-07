import * as React from "react";
import Typography from '@mui/material/Typography';
import { Box, Chip, Grid, Tooltip } from '@mui/material'
import { AppContext, AppContextType } from "../../pages/_app";
import { useQuery, useQueryClient } from "react-query";
import { addPlayListToQueue, fetchPlaylistCollection } from "../../api";
import { millisToMinutesAndSeconds } from "../../utils";
import Link from "next/link";
import { Bookmark, PlayCircleFilledRounded } from "@mui/icons-material";
import PlayListSongs from "./PlayListSongs";
import Image from "next/image";
import { useRouter } from "next/router";
import FavoriteIcon from '@mui/icons-material/Favorite';

interface CompProps {
    playlist: string
}
function PlayListCollectionComp(props: CompProps) {

    const { playlist } = props
    const router = useRouter();
    // console.log("From Playlist", parseInt(router.query.user_playlist as string))
    const {data,isLoading,isError,error,} = useQuery(["playlist_collection", playlist],async () =>  await fetchPlaylistCollection(playlist));
    const { user, setCurrentSong } = React.useContext(
        AppContext
    ) as AppContextType;
    const queryClient = useQueryClient()
    const playAllSongs = async () => {
        if (user && user.id && data) {
            await addPlayListToQueue(user.id, data.songs.map(x => x.id))
            setCurrentSong(data.songs[0])
            await queryClient.refetchQueries(['user', user.id])
            await queryClient.refetchQueries(['queue', user.id])
        } else {
            console.error('No Error is found')
        }
    }
    const likePlayList = async () => {
        // if (user && user.id && data) {
        //     await addPlayListToQueue(user.id, data.songs.map(x => x.id))
        //     setCurrentSong(data.songs[0])
        //     await queryClient.refetchQueries(['user', user.id])
        // } else {
        //     console.error('No Error is found')
        // }
    }

    const followPlayList = async () => {
        // if (user && user.id && data) {
        //     await addPlayListToQueue(user.id, data.songs.map(x => x.id))
        //     setCurrentSong(data.songs[0])
        //     await queryClient.refetchQueries(['user', user.id])
        // } else {
        //     console.error('No Error is found')
        // }
    }
    const img = data?.image && data.image !== 'no-cover.jpg' ? data?.image : '/cover.jpg'

    return <Box mt={4} >
        <Grid container alignItems='center' columnGap={4} mb={4}>
            <Grid item width={450} height={300} sx={{ background: 'black' }}>
                <Image className='artist_avatarkh'  src={data?.image && data.image !== 'no-cover.jpg' ? data?.image : '/cover.jpg'} width={450} height={300} />
            </Grid>
            <Grid item>
                <Typography fontSize="x-large">{data?.title?.toLocaleUpperCase()}</Typography>
                <Typography fontSize="medium">{data?.artists?.slice(0, 4).map(x => <Link href={`artist/${x.id}`} key={'artist' + x.id}>{x.name + '  '}</Link>)} {data?.artists?.length! > 4 && <span>and more</span>}</Typography>
                <Typography fontSize="medium"><Chip label={data?.songs.length + ' songs'} /> <Chip label={millisToMinutesAndSeconds(data?.songs?.reduce((acc, x) => acc += x.duration, 0) || 0) + ' mins'} />   </Typography>
                <Grid container columnGap={4} sx={{ marginTop: 5 }}>
            <Grid item>
                <Tooltip title="play all">
                    <PlayCircleFilledRounded fontSize="large" onClick={playAllSongs} sx={{ cursor: 'pointer' }} className="play-all-songs" />
                </Tooltip>
                <Tooltip title="follow playlist">
                    <Bookmark fontSize="large" onClick={followPlayList} sx={{ cursor: 'pointer' }} className="play-all-songs" />
                </Tooltip>
                <Tooltip title="like">
                    <FavoriteIcon fontSize="large" onClick={likePlayList} sx={{ cursor: 'pointer' }} className="play-all-songs" />
                </Tooltip>
            </Grid>
        </Grid>
            </Grid>
        </Grid>
        <PlayListSongs data={data?.songs || []} playlist_collection={true}/>
    </Box>
}

function moviePropsAreEqual(
    prevProps: CompProps,
    nextProps: CompProps
) {
    ////console.log("Sidhu",prevProps,nextProps);
    return prevProps === nextProps;
}

export const PlayListCollection = React.memo(
    PlayListCollectionComp,
    moviePropsAreEqual
);
