import * as React from 'react';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import { AppContext, AppContextType } from '../../pages/_app';
import { markAsBadLink, liekdislikestation } from '../../api';
import { Snackbar } from '@mui/material';
import { useMutation, useQueryClient } from 'react-query';
import { Favorite } from '@mui/icons-material';
import { RadioStation } from '../../types/RadioStationType';
import { checkIfUserHasLikedRadio } from '../../utils';

export default function RadioConTextMenu({ children, Radio, RadioId }: { children: React.ReactElement, Radio: RadioStation | undefined, RadioId: string | undefined }) {
    const queryClient = useQueryClient()
    const [contextMenu, setContextMenu] = React.useState<{
        mouseX: number;
        mouseY: number;
    } | null>(null);
    const [openSnack, setopenSnack] = React.useState<boolean>(false)
    const [message, setMessage] = React.useState<string>('')
    const { user, setstationForAddToPlc, setopenAddToPlayListMenu, setUser } = React.useContext(AppContext) as AppContextType
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

   

    const hadleAddtoplaylist = () => {
        if (Radio) {
            setstationForAddToPlc(Radio)
            setopenAddToPlayListMenu(true)
        }
        handleClose()
    }

    async function reportLink() {
        if (Radio) {
            await markAsBadLink(Radio.id, true)
        }

        handleClose('Sorry for your inconvenience, Radio Reported As Not Playable.We will rectify as soon as possible.For contributing by uploading Radio by yourself please contact us at sunapusiddharth3@gmail.com. Thanks!!!')
    }
    

    async function likeRadio() {
        if (Radio)
            saveToLikedRadios.mutate({ Radioid: Radio.id, likedislike: checkIfUserHasLikedRadio(user, Radio.id) })
        handleClose('Added to Favourite')

    }
    const saveToLikedRadios = useMutation(({ Radioid, likedislike }: { Radioid: string, likedislike: boolean }) => liekdislikestation(user?.id || '1', likedislike, Radioid), {
        onSuccess: (data) => {
            queryClient.setQueryData(['user', { id: user?.id }], data)
            setUser(data)
            console.log("OnSuccess", user?.id, data)
        }
    })



    return (
        <div onContextMenu={handleContextMenu} style={{ cursor: 'context-menu' }}>
            {children}
            <Menu
                sx={{ height: '126px' }}
                open={contextMenu !== null}
                onClose={() => handleClose()}
                anchorReference="anchorPosition"
                anchorPosition={
                    contextMenu !== null
                        ? { top: contextMenu.mouseY, left: contextMenu.mouseX }
                        : undefined
                }
            >
                <MenuItem onClick={likeRadio}><Favorite sx={{marginRight:2}}/> Add To Fav</MenuItem>
                {/* <MenuItem onClick={hadleAddtoplaylist}><PlaylistAdd sx={{marginRight:2}}/> Add To Playlist</MenuItem> */}
                {/* <MenuItem onClick={reportLink}><BrokenImage sx={{marginRight:2}}/> Not Working</MenuItem> */}
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
