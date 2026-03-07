import * as React from 'react';
import Box from '@mui/material/Box';
import Drawer from '@mui/material/Drawer';
import Typography from '@mui/material/Typography';
import { PodcastEpisodeDto } from '../../types/PodcastEpisode.dto';
import { PodcastCardDto } from '../../types/PodcastCard.dto';
import Grid from '@mui/material/Unstable_Grid2'; // Grid version 2
import { Button, Divider, IconButton, styled, useTheme } from '@mui/material';
import { millisToMinutesAndSeconds } from '../../utils';
import { AppContext, AppContextType } from '../../pages/_app';
import { userListenedPodcasts } from '../../api';

import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
const drawerWidth = 240;



export default function PersistentDrawerRight({ selectedSong, podcast, open, setOpen }: { selectedSong: PodcastEpisodeDto, podcast: PodcastCardDto, open: boolean, setOpen: (d: boolean) => void }) {
    const theme = useTheme();
    const { user, setPodcast } = React.useContext(
        AppContext
    ) as AppContextType;
    async function playEpisode(song: PodcastEpisodeDto | undefined) {

        if (song && podcast) {
            setPodcast({ episode: song, podcast })
            await userListenedPodcasts(user?.id!, podcast?.uuid!, 0, song.uuid)
        }

    }
    function addToWatchLater() {
        if (selectedSong) {
            //make api call to save this data
            // setPodcast(selectedSong)
        }
    }
    const DrawerHeader = styled('div')(({ theme }) => ({
        display: 'flex',
        alignItems: 'center',
        padding: theme.spacing(0, 1),
        // necessary for content to be below app bar
        ...theme.mixins.toolbar,
        justifyContent: 'flex-start',
    }));

    return (
        <Drawer
            sx={{
                width: drawerWidth,
                flexShrink: 0,
                '& .MuiDrawer-paper': {
                    width: drawerWidth,
                },
            }}
            variant="persistent"
            anchor="right"
            open={open}
        >
            <DrawerHeader>
                <IconButton onClick={()=>setOpen(false)}>
                    {theme.direction === 'rtl' ? <ChevronLeftIcon /> : <ChevronRightIcon />}
                </IconButton>
            </DrawerHeader>
            <Divider />
            <Box sx={{ minHeight: 600 }} pt={2}>
                <Box>
                    <img src={selectedSong?.image_url} width={300} height={200} />
                    <Typography>{selectedSong?.name}</Typography>
                    <Typography>{selectedSong?.date_published ? new Date(selectedSong?.date_published).toDateString() : ''}</Typography>
                    <Typography> {selectedSong?.duration ? millisToMinutesAndSeconds(selectedSong?.duration) : ''} </Typography>
                </Box>
                <Grid sx={{ marginTop: 2 }} flexDirection="row" justifyContent={'flex-start'} columnGap={2} display="flex">
                    <Button variant="contained" onClick={() => playEpisode(selectedSong)}>PLAY</Button>
                    <Button variant="contained" onClick={addToWatchLater}>ADD TO WATCH LATER</Button>
                </Grid>
                <Box mt={2}>
                    <div dangerouslySetInnerHTML={{ __html: selectedSong.description?.replace(/(href=["'])([A-Za-z-\\\/:.]*)(["'])/ig, "") }} />

                </Box>
            </Box>
        </Drawer>
    );
}
