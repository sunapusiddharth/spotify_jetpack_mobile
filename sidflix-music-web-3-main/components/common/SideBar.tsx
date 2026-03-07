import * as React from 'react';
import List from '@mui/material/List';
import { AccountCircle, QueueMusic, HomeMax, Addchart, Delete, Radio, Podcasts, TrackChanges, HeatPumpSharp, Favorite, Search, LibraryMusic, PlaylistPlay } from '@mui/icons-material';
import Link from "next/link";;
import { UserType } from '../../types/User.type';
import CreateNewPlayListModal from './CreateNewPlayList';
import { Box, Icon, IconButton, Tooltip, Typography } from '@mui/material';
import { deletePlayList, fetchUserData } from '../../api';
import { useQuery, useQueryClient } from 'react-query';
import { AppContext, AppContextType } from '../../pages/_app';


interface CompProps {
    user: UserType | undefined;
}

export const editor_user_id = '038f20a8-6454-482f-9fec-1473c33f74b1'

function SideBarComp(props: CompProps) {
    const { user } = React.useContext(
        AppContext
    ) as AppContextType;
    const [open, setOpen] = React.useState(false);
    const queryClient = useQueryClient()

    let menuitems = [
        { name: 'Discover', icon: <img src="/" width={100} height={100} />, path: '/' },
        { name: 'Search', icon: <Search />, path: '/search' },
        { name: 'New Releases', icon: <TrackChanges />, path: '/available_tracks' },
        { name: 'Popular Albums', icon: <LibraryMusic />, path: '/albums' },
        { name: 'Popular Artists', icon: <AccountCircle />, path: '/artist' },
        { name: 'Top PlayLists', icon: <PlaylistPlay />, path: '/playlist_collection' },
        { name: 'Radio', icon: <Radio />, path: '/radio' },
        { name: 'Podcasts', icon: <Podcasts />, path: '/podcast' },
    ]
    editor_user_id == user?.id && menuitems.push({ name: 'Upload Songs', icon: <AccountCircle />, path: '/upload' })

    const createNewPlayList = () => {
        setOpen(true)
    }
    const deletePlayListHandler = async (playlistid: string) => {
        if (!user?.id) return
        await deletePlayList(user.id, playlistid)
        await queryClient.invalidateQueries(['user', user.id])
        await queryClient.invalidateQueries(['user_playlist', user.id])
        await queryClient.invalidateQueries(['playlist', playlistid])
        console.log("Delete", user.name)
    }
    const { data } = useQuery(["user", user?.id], async () => await fetchUserData(user?.name!));
    let playlistitems = data?.playlists?.map(x => ({ id: x.id, name: x.name, icon: <QueueMusic />, path: `/playlist/${x.id}` })) || []
    playlistitems = [{ id: 'liked_songs', name: 'Your Fav Songs', icon: <Favorite />, path: `/fav` }, ...playlistitems]
    const drawer = (
        <div className='drawesidebar' style={{ height: '100%', overflowY: 'scroll', padding: 10 }}>
            <List sx={{ padding: { xs: 1, md: 0 } }}>
                {menuitems.map((x, index) => <Link href={x.path} key={'menu' + index}>
                    <Box sx={{ display: 'flex', columnGap: 1, alignItems: 'center', }}>
                        <Tooltip title={x.name}>
                            <Icon sx={{ minWidth: 27, color: '#39FF14' }} fontSize="small">
                                {x.icon}
                            </Icon>
                        </Tooltip>

                    </Box>
                </Link>)}
            </List>
            <List sx={{ display: 'flex', justifyContent: 'space-around', alignItems: 'center', marginTop: 5 }} >
                <Typography sx={{
                    '&:hover': {
                        backgroundColor: 'white',
                        color: 'black',
                        cursor: 'pointer'
                    },
                }}>PlayList</Typography>
                <IconButton aria-label="createnewplaylist" onClick={createNewPlayList}>
                    <Tooltip title="Create new Playlist">
                        <Addchart fontSize='large' sx={{ fontSize: '1.5rem !important' }} />
                    </Tooltip>
                </IconButton>
            </List>
            <List sx={{ overflowY: 'scroll', height: 300 }}>
                {playlistitems.map((x, index) => <Link href={x.path} key={'libraryitems' + index} ><Box display="flex" sx={{ alignItems: 'center', justifyContent: 'space-around' }} pr={2} pl={1} >
                    <Box display="flex" sx={{ alignItems: 'center', flex: 2, columnGap: 1 }}>
                        <Typography style={{ fontSize: 'medium', color: '#39FF14' }} >{x.name}</Typography>
                    </Box>
                    {x.id !== 'liked_songs' &&
                        <IconButton onClick={() => deletePlayListHandler(x.id)}>
                            <Delete fontSize="small" />
                        </IconButton>}
                </Box>
                </Link>)}
            </List>
        </div >
    );


    return (
        <>
            {drawer}
            <CreateNewPlayListModal open={open} setOpen={setOpen} />
        </>
    );
}

function moviePropsAreEqual(
    prevProps: CompProps,
    nextProps: CompProps
) {
    return prevProps === nextProps;
}

export const SideBar = React.memo(
    SideBarComp,
    moviePropsAreEqual
);

