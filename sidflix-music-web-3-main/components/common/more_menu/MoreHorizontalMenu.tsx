import * as React from "react";
import MenuItems from "./MenuItems";
import { MoreHoriz } from '@mui/icons-material';
import { IconButton } from "@mui/material";

interface COompProps {
    song_artists: { title: string, id: string, path: string }[],
    user_playlist: { title: string, id: string }[],
    addToQueue: any,
    saveToLikedSongs: any,
    album_path: string,
    addToPlaylist: any,
    copyShareLink: any
}
export interface SubMenusType {

    title: string,
    type: 'link' | 'playlist' | 'text',
    id: string,
    onclick?: any
    path?: string

}
export interface MenuItemType {
    title: string,
    onClick?: any,
    link?: string,
    submenu?: SubMenusType[]
}
const MoreHorizontalMenu: React.FC<COompProps> = (
    props: COompProps
) => {
    const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
    const open = Boolean(anchorEl);
    const handleClick = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };
    const handleClose = () => {
        setAnchorEl(null);
    };
    const menuContent: MenuItemType[] = [
        {
            title: "Add to Queue",
            onClick: props.addToQueue
        },
        {
            title: "Go to artist",
            submenu: props.song_artists.map(x => ({
                title: x.title,
                type: 'link',
                id: x.id,
                path: x.path
            }))
        },
        {
            title: "Go to album",
            link: props.album_path
        },
        {
            title: "Save to your liked songs",
            onClick: props.saveToLikedSongs
        },

        {
            title: "Save to playlist",
            submenu: props.user_playlist?.map(x => ({
                title: x.title,
                type: 'playlist',
                id: x.id,
                onclick: props.addToPlaylist
            }))
        },
        {
            title: "Share",
            submenu: [{
                title: 'Copy Song Link',
                type: 'text',
                id: '',
                onclick: props.copyShareLink
            }]
        },
    ];
    return (
        <>
            <IconButton
                aria-label="more"
                id="long-button"
                aria-controls={open ? 'long-menu' : undefined}
                aria-expanded={open ? 'true' : undefined}
                aria-haspopup="true"
                onClick={handleClick}
            >
                <MoreHoriz />
            </IconButton>
            <nav>
                <ul className="menus">
                    {menuContent.map((menu, index) => {
                        const depthLevel = 0;
                        return <MenuItems items={menu} key={index} depthLevel={depthLevel} onClick={menu.onClick} />;
                    })}
                </ul>
            </nav>
        </>
    );
};

export default MoreHorizontalMenu;
