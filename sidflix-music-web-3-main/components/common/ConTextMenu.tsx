import * as React from 'react';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import { SongType } from '../../types/Song.type';
import { AppContext, AppContextType } from '../../pages/_app';
import { AllTracksPageInfo, deleteSong, liekdislikesong, markAsBadLink, requestTrackAddition } from '../../api';
import { Snackbar } from '@mui/material';
import { InfiniteData, useMutation, useQueryClient } from 'react-query';
import { checkIfUserHasLikedSong } from '../../utils';
import { BrokenImage, DeleteForever, Favorite, PlayCircle, PlaylistAdd, RequestPage } from '@mui/icons-material';
import { editor_user_id } from './SideBar';

export default function ContextMenu({ children, song, songId }: { children: React.ReactElement, song: SongType | undefined, songId: string | undefined }) {
    const queryClient = useQueryClient()
    const [contextMenu, setContextMenu] = React.useState<{
        mouseX: number;
        mouseY: number;
    } | null>(null);
    const [openSnack, setopenSnack] = React.useState<boolean>(false)
    const [message, setMessage] = React.useState<string>('')
    const { user, setCurrentSong, setsongForAddToPlc, setopenAddToPlayListMenu, setUser } = React.useContext(AppContext) as AppContextType
    const handleContextMenu = (event: React.MouseEvent) => {
        event.preventDefault();
        setContextMenu(
            contextMenu === null
                ? {
                    mouseX: event.clientX + 2,
                    mouseY: event.clientY - 6,
                }
                : // repeated contextmenu when it is already open closes it with Chrome 84 on Ubuntu
                // Other native context menus might behave different.
                // With this behavior we prevent contextmenu from the backdrop to re-locale existing context menus.
                null,
        );
    };

    const handleClose = (message?: string) => {
        setContextMenu(null);
        if (message) {
            setMessage(message)
            setopenSnack(true)
        }
    };

    function playSong() {
        if (song)
            setCurrentSong(song)
        handleClose()
    }

    const hadleAddtoplaylist = () => {
        if (song) {
            setsongForAddToPlc(song)
            setopenAddToPlayListMenu(true)
        }
        handleClose()
    }

    async function reportLink() {
        if (song) {
            await markAsBadLink(song.id, true)
            handleClose('Sorry for your inconvenience, Song Reported As Not Playable.We will rectify as soon as possible.For contributing by uploading song by yourself please contact us at sunapusiddharth3@gmail.com. Thanks!!!')

        }

    }
    async function requestTrack() {
        if (song) {
            try {
                const res = await requestTrackAddition(user?.id!, song.id)
                if (res == 'success') {
                    setMessage('Your song is now available')
                }
                if (res == 'failed') {
                    setMessage('Song was requested , there was a problem in downloading ! we will upload it by end of day.')
                }
                if (res == 'failed2') {
                    setMessage('Song was not requested successfully')
                }
                handleClose('Track has been requested, will be added in few hours. For contributing by uploading song by yourself please contact us at sunapusiddharth3@gmail.com. Thanks!!!')
            } catch (error) {
                handleClose('Something went wrong please try later.')
            }
        }
    }

    async function likeSong() {
        if (song) {
            await saveToLikedSongs.mutate({ songid: song.id, likedislike: checkIfUserHasLikedSong(user, song.id) })
            handleClose('Added to Favourite')
        }
    }

    const saveToLikedSongs = useMutation(({ songid, likedislike }: { songid: string, likedislike: boolean }) => liekdislikesong(user?.id || '1', likedislike, songid), {
        onSuccess: (data) => {
            queryClient.setQueryData(['user', { id: user?.id }], data)
            setUser(data)
            console.log("OnSuccess", user?.id, data)
        }
    })

    async function deleteSongFunc() {
        if (song) {
            await deleteSongFn.mutate({ songid: song.id })
            handleClose('Song Deleted')
        }
    }

    const deleteSongFn = useMutation(({ songid }: { songid: string }) => deleteSong(songid), {
        onSuccess: (data) => {
            const res: InfiniteData<AllTracksPageInfo> | undefined = queryClient.getQueryData(['getAllAvailableSongs'])
            if (!res) return undefined
            const newPagesArray = res?.pages.map((page) => {
                const idx = page.results.findIndex(x => x.id == songId)
                if (idx !== -1) {
                    delete page.results[idx]
                }
                return page
            })
            queryClient.setQueryData<InfiniteData<AllTracksPageInfo>>('getAllAvailableSongs', {
                pages: newPagesArray,
                pageParams: res.pageParams,
            })
        }
    })

    return (
        <div onContextMenu={handleContextMenu} style={{ cursor: 'context-menu' }}>
            {children}
            <Menu
                sx={{ height: '326px' }}
                open={contextMenu !== null}
                onClose={() => handleClose()}
                anchorReference="anchorPosition"
                anchorPosition={
                    contextMenu !== null
                        ? { top: contextMenu.mouseY, left: contextMenu.mouseX }
                        : undefined
                }
            >
                <MenuItem onClick={likeSong}><Favorite sx={{ marginRight: 2 }} /> Add To Fav</MenuItem>
                <MenuItem onClick={playSong}><PlayCircle sx={{ marginRight: 2 }} /> Play</MenuItem>
                <MenuItem onClick={hadleAddtoplaylist}><PlaylistAdd sx={{ marginRight: 2 }} /> Add To Playlist</MenuItem>
                <MenuItem onClick={reportLink}><BrokenImage sx={{ marginRight: 2 }} /> Link Not Working</MenuItem>
                <MenuItem onClick={requestTrack}><RequestPage sx={{ marginRight: 2 }} /> Request Song</MenuItem>
                {user?.id == editor_user_id ? <MenuItem onClick={deleteSongFunc}><DeleteForever sx={{ marginRight: 2 }} /> Delete</MenuItem> : <></>}
            </Menu>
            <Snackbar
                anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
                open={openSnack}
                onClose={() => setopenSnack(false)}
                message={message}
                key='snackbar'
                autoHideDuration={1500}
            />
        </div>
    );
}
