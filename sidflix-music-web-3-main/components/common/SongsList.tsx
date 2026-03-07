import { Addchart, Edit, Favorite, MoreVert } from "@mui/icons-material";
import { Grid, IconButton, List, ListItem, ListItemAvatar, ListItemText, Menu, MenuItem, Tooltip, Typography } from "@mui/material";
import Link from "next/link";
import * as React from "react";
import { liekdislikesong } from "../../api";
import { AppContext, AppContextType } from "../../pages/_app";
import { SongType } from "../../types/Song.type";
import { checkIfUserHasLikedSong, millisToMinutesAndSeconds } from "../../utils";
import UplaodTrackModal from "./UploadTrack";

const SongsList = (
    { songs, withImage, bigImage, restricttHeight }: { songs: SongType[], withImage: boolean, bigImage: boolean, restricttHeight: boolean }
) => {
    // const isMobile = width < 600 ? true : false
    const { setCurrentSong, user } = React.useContext(
        AppContext
    ) as AppContextType;
    const [selectedSong, setSelectedSong] = React.useState('');
   
   
    const handleSelectSong = (song: SongType) => {
        setSelectedSong(song.id);
        setCurrentSong(song)
    }

    
    return (
        <>
            {/* <List sx={restricttHeight ? { marginTop: 2, maxHeight: '50vh', overflowY: 'scroll' } : { marginTop: 2, overflowY: 'scroll' }} >
                <ListItem >
                    {withImage && <ListItemAvatar></ListItemAvatar>}
                    <ListItemText sx={{ width: 500, paddingRight: 3 }} primary='Title'></ListItemText>
                    <ListItemText primary={'Duration'} sx={{ width: 100 }} />
                    <ListItemText primary={''} sx={{ width: 100 }} />
                </ListItem>
                {songs?.map((song, index) => <ListItem sx={{ height: 80 }} key={'playlist-song-' + index} className={selectedSong == song.id ? 'selected_song' : 'playlist_song'} >
                    <span onClick={() => handleSelectSong(song)} style={{ display: 'flex', flexDirection: 'row', alignItems: 'center' }}>{withImage && <ListItemAvatar >
                        <img
                            src={song.thumbnail !== 'no-cover.jpg' ? song.thumbnail : '/sample.jpg'}
                            alt="album-cover"
                            width={bigImage ? 50 : 30}
                            height={bigImage ? 50 : 30}
                        />
                    </ListItemAvatar>}
                        <ListItemText sx={{ width: 500, paddingRight: 3, paddingLeft: 3 }} >
                            <Grid item sx={{ overflow: "hidden", textOverflow: "ellipsis" }}><Typography variant={song.name.length > 10 ? 'body1' : 'body2'} noWrap>{song.name}</Typography>
                                <Typography variant="body2">
                                    {song.artists.map(x => <Link href={`/artist/${x.id}`} key={x.id}>{x.title}</Link>)}
                                </Typography></Grid>
                        </ListItemText>
                    <ListItemText primary={millisToMinutesAndSeconds(song.duration)} sx={{ width: 100 }} /></span>
                    {isMobile ? <ContextmenuSong saveToLikedSongs={saveToLikedSongs} song={song} user={user} hadleAddtoplaylist={hadleAddtoplaylist} /> : <> <ListItemText>
                        <Favorite fontSize="medium" onClick={() => saveToLikedSongs.mutate({ songid: song.id, likedislike: checkIfUserHasLikedSong(user, song.id) })} className={!checkIfUserHasLikedSong(user, song.id) ? 'user-liked-song' : 'play-comp-icons'} />
                    </ListItemText>
                        <IconButton aria-label="createnewplaylist" onClick={() => hadleAddtoplaylist(song)}>
                            <Addchart fontSize='large' sx={{ fontSize: '1.5rem !important' }} />
                        </IconButton>
                        <Tooltip title="edit track's mp3 file">
                            <IconButton aria-label="editTrack" onClick={() => handleEditSong(song)}>
                                <Edit fontSize='large' sx={{ fontSize: '1.5rem !important' }} />
                            </IconButton>
                        </Tooltip>
                    </>
                    }
                </ListItem>
                )}
            </List>
            
            <UplaodTrackModal open={openEditModal} setOpen={setopenEditModal} song={editSong} /> */}
        </>
    );
};

export default SongsList;

const ContextmenuSong = ({ saveToLikedSongs, song, user, hadleAddtoplaylist }: { saveToLikedSongs: any, song: SongType, user: any, hadleAddtoplaylist: any }) => {
    const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
    const open = Boolean(anchorEl);
    const handleClick = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };
    const handleClose = () => {
        setAnchorEl(null);
    };
    const ITEM_HEIGHT = 48;

    return <div>
        <IconButton
            aria-label="more"
            id="long-button"
            aria-controls={open ? 'long-menu' : undefined}
            aria-expanded={open ? 'true' : undefined}
            aria-haspopup="true"
            onClick={handleClick}
        >
            <MoreVert />
        </IconButton>
        <Menu
            id="long-menu"
            MenuListProps={{
                'aria-labelledby': 'long-button',
            }}
            anchorEl={anchorEl}
            open={open}
            onClose={handleClose}
            PaperProps={{
                style: {
                    maxHeight: ITEM_HEIGHT * 4.5,
                    width: '20ch',
                },
            }}
        >
            <MenuItem key={'Favourite'} onClick={handleClose}>
                <Typography>Like Song</Typography>
                <Favorite fontSize="medium" onClick={() => saveToLikedSongs.mutate({ songid: song.id, likedislike: checkIfUserHasLikedSong(user, song.id) })} className={!checkIfUserHasLikedSong(user, song.id) ? 'user-liked-song' : 'play-comp-icons'} />
            </MenuItem>
            <MenuItem key={'Add to playlist'} onClick={handleClose}>
                <Typography>Add to Playlist</Typography>
                <Addchart fontSize='large' sx={{ fontSize: '1.5rem !important' }} onClick={() => hadleAddtoplaylist(song)} />
            </MenuItem>
        </Menu>
    </div>
}