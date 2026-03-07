import * as React from "react";
import { useQuery } from "react-query";

import router from "next/router";
import { Box, CircularProgress, Drawer, Grid, List, ListItem, Typography } from "@mui/material";
import { fetchQueueData } from "../api";
import { useSession } from "next-auth/react";
import { AppContext, AppContextType } from "../pages/_app";
import Image from 'next/image'

export const QueueComp = () => {
    const { data: session } = useSession()
    React.useEffect(() => {
        if (!session) {
            router.push("/auth/signin");
        }
    }, []);

    const { song, setCurrentSong, user, openQueue, setopenQueue } = React.useContext(
        AppContext
    ) as AppContextType;
    const {
        data,
        isLoading,
        isError,
        error
    } = useQuery(
        [openQueue],
        async () => await fetchQueueData(user?.id || 'test'),
        {
            keepPreviousData: true,
            refetchOnMount: false,
            refetchOnWindowFocus: false,
        }
    );

    return (
        <Drawer
            anchor={'right'}

            open={openQueue}
            onClose={() => setopenQueue(false)}
            PaperProps={{
                sx: {
                    background: '#22232d',
                    width: 400,
                    pl: 1,
                    pr: 1
                }
            }}
        >
            <Box sx={{
                overflowY: 'scroll'
            }}>
                {isLoading ? <CircularProgress size={20} /> : <>
                    < Typography variant="body2" sx={{ marginTop: 5, pl: 2 }}>Now Playing</Typography>
                    <List sx={{ p: 2 }} >
                        <ListItem sx={{
                            backgroundColor: '#0093E9',
                            backgroundImage: 'linear-gradient(160deg, #0093E9 0%, #80D0C7 100%)',
                            padding: 2,
                        }}>

                            <Grid container spacing={3} flexWrap="nowrap">
                                <Grid item><Image src="/sample.jpg"
                                    alt="Picture of the author"
                                    width={50}
                                    height={50}
                                /></Grid>
                                <Grid item><Typography variant="caption">{song?.name}</Typography>
                                    <Typography variant="caption">{song?.artists?.map(x => x.title).toString()}</Typography></Grid>
                            </Grid>
                        </ListItem>
                        <Typography variant="subtitle2" sx={{ pt: 2 }}>Up Next</Typography>
                        {data?.map((song, index) => <ListItem key={'playlist-song-' + index} onClick={() => setCurrentSong(song)} className={'playlist_song'}
                            sx={{
                                '&:hover': {
                                    backgroundColor: '#0093E9',
                                    // backgroundImage: 'linear-gradient(160deg, #0093E9 0%, #80D0C7 100%)'
                                },
                                p: 2,
                            }}>
                            <Grid container spacing={3} flexWrap="nowrap">
                                <Grid item><Image src={song.thumbnail && song.thumbnail !== 'no-cover.jpg' ? song.thumbnail : "/sample.jpg"}
                                    alt="Picture of the author"
                                    width={60}
                                    height={60}
                                /></Grid>
                                <Grid item display={"flex"} flexDirection="column">
                                    <Typography sx={{ fontSize: 'small' }}>{song.name}  {song.duration}</Typography>
                                    <Typography sx={{ fontSize: 'x-small', color: 'whitesmoke' }} mt={1} >Album {song?.album}</Typography>
                                </Grid>
                            </Grid>
                        </ListItem>)}
                    </List></>
                }
            </Box>

        </Drawer >
    );
};

