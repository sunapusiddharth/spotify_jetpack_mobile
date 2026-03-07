import { Grid } from "@mui/material";
import * as React from "react";
import { AppContext, AppContextType } from "../../pages/_app";
import { SongType } from "../../types/Song.type";
import { UserPlayLists } from "../common/UserPlayListModal";
import { ContentCard } from "../home/Card";
interface COompProps {
    data: SongType[],
    playlist_collection?: boolean,
    bigImage?: boolean
}


const PlayListSongs: React.FC<COompProps> = (
    props: COompProps
) => {
    const { setCurrentSong } = React.useContext(
        AppContext
    ) as AppContextType;
    const handleSelectSong = (song: SongType) => {
        // console.log("inside handleSelectSong")
        setCurrentSong(song)
    }
    const [addToPlayListSong, setaddToPlayListSong] = React.useState<SongType>();
    const [open, setOpen] = React.useState<boolean>(false);
    const hadleAddtoplaylist = (song: SongType) => {
        // console.log("inside hadleAddtoplaylist")
        setaddToPlayListSong(song)
        setOpen(true)
    }

    return (
        <>
         <Grid spacing={2} container mt={4}>
          {props.data?.map((song, index) => <Grid item key={"available_tracks" + song.id + index}><ContentCard  data={{
            image: song.thumbnail,
            id: song.id,
            title: song.name,
            subtitle: '',
            type: "music_card",
            path: "",
            song: song
          }} /></Grid>)}
        </Grid>
        
            {/* <List sx={{ overflowY: 'scroll' }} >
                <ListItem>
                    <ListItemText primary="#TITLE" sx={{ width: 300 }} />
                    {!props.playlist_collection ? <ListItemText primary="ALBUM" sx={{ width: 100 }} className='pl_hide_artist' /> : ''}
                    <ListItemText primary="DURATION" sx={{ width: 100 }} />
                    <ListItemText primary="ARTISTS" sx={{ width: 100 }} />
                </ListItem>
                {props.data.map((song, index) => <ContextMenu song={song} songId={song.id}><ListItem key={'playlist-song-' + index} sx={{height:70}}
                >
                    <ListItemText sx={{ width: 300}} onClick={() => handleSelectSong(song)} className='playlist_song'>
                        <Grid container spacing={3} flexWrap='nowrap'>
                            <Grid item>
                                <Image
                                    src={song.thumbnail && song.thumbnail !== 'no-cover.jpg' ? song.thumbnail : '/sample.jpg'}
                                    alt="Picture of the author"
                                    width={80}
                                    height={60}
                                /></Grid>
                            <Grid item sx={{ overflow: "hidden", textOverflow: "ellipsis", }}><Typography variant={song.name?.length > 10 ? 'body2' : 'body2'} noWrap sx={{ paddingTop: 1 }}>{song.name}</Typography>
                                <Typography variant="body2" >
                                    {song.artists?.slice(0, 5)?.map(x => <Link href={`/artist/${x.id}`} key={x.id}>{x.title + '   '}</Link>)}
                                </Typography></Grid>
                        </Grid>
                    </ListItemText>
                    {!props.playlist_collection && song.album ? <ListItemText sx={{ width: 100 }} className='pl_hide_artist'>
                        <Link href={`/playlist/${song.album?.id}`}>{song.album?.title}</Link>
                    </ListItemText> : ''}
                    {!props.playlist_collection && song.artists?.length ? <ListItemText sx={{ width: 100 }} className='pl_hide_artist'>
                        {song.artists.map(x => <Link href={`/artist/${x.id}`} key={x.id}>{x.title}</Link>)}
                    </ListItemText> : ''}
                    <ListItemText primary={millisToMinutesAndSeconds(song.duration)} sx={{ width: 100 }} />
                    
                </ListItem></ContextMenu>)}
            </List> */}
            <UserPlayLists open={open} setOpen={setOpen} song={addToPlayListSong} />
        </>
    );
};

{/* <IconButton aria-label="addtoplaylist" onClick={() => hadleAddtoplaylist(song)}>
                        <Addchart fontSize='large' sx={{ fontSize: '1.5rem !important' }} />
                    </IconButton> */}
export default PlayListSongs;
