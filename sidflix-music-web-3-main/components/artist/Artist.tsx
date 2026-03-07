import * as React from "react";
import Typography from '@mui/material/Typography';
import { Box, Grid, IconButton, Tooltip } from '@mui/material'
import { PlayCircleFilledRounded, Refresh, SpeakerGroup } from "@mui/icons-material";
import { ArtistType } from "../../types/ArtistType";
import Image from 'next/image'
import { addPlayListToQueue } from "../../api";
import { AppContext, AppContextType } from "../../pages/_app";
import { SongType } from "../../types/Song.type";
import { useQueryClient } from "react-query";
interface CompProps {
    artist: ArtistType,
    songs: SongType[]
}
function ArtistCoverComp(props: CompProps) {
    const { user, setCurrentSong } = React.useContext(
        AppContext
    ) as AppContextType;
    const queryClient = useQueryClient()
    const playAllSongs = async () => {
        if (user && user.id) {
            await addPlayListToQueue(user.id, props.songs.map(x => x.id))
            await queryClient.refetchQueries(['queue', user.id])
            setCurrentSong(props.songs[0])
            //refetch user 
            await queryClient.refetchQueries(['user', user.id])
        } else {
            console.error('No Error is found')
        }
    }
    const likeplayList = () => {

    }
    const { artist } = props
    return <Box>
        <Grid container alignItems='center' columnGap={4}>
            <Grid item width={150} height={150} sx={{ background: 'black' }}>
                <Image alt="" src={artist.image && artist.image !== 'no-cover.jpg' ? artist.image : "/sample.jpg"} width={250} height={250} />
            </Grid>
            <Grid item>
                <Typography fontSize="x-large">{artist.title}</Typography>
                <Typography fontSize="small">{artist.genres?.join(',')}</Typography>
                {artist.monthly_listeners ? <Typography fontSize="medium">{artist.monthly_listeners} monthly listeners</Typography> : ''}
                <Box sx={{ display: 'flex' }}>
                    <IconButton onClick={playAllSongs}>
                        <Tooltip title="play all">
                            <SpeakerGroup onClick={playAllSongs} />
                        </Tooltip>
                        <Typography variant='caption'>PLAY ALL</Typography>
                    </IconButton>

                </Box>
            </Grid>
        </Grid>
    </Box>
}

function moviePropsAreEqual(
    prevProps: CompProps,
    nextProps: CompProps
) {
    ////console.log("Sidhu",prevProps,nextProps);
    return prevProps === nextProps;
}

export const ArtistCover = React.memo(
    ArtistCoverComp,
    moviePropsAreEqual
);
