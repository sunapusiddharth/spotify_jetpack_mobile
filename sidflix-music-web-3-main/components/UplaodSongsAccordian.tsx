import * as React from 'react';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import Typography from '@mui/material/Typography';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { SongType } from '../types/Song.type';
import { Box, List, ListItem, ListItemText } from '@mui/material';
import SearchsongByName from './SongploadName';
import { findSongByName } from '../api';
import { AppContext, AppContextType } from '../pages/_app';
import ReactPlayer from 'react-player';

export default function UplaodSongAccordian({ selectedSong, index, suggestions, selectsuggestionFofFile, uploadedsong, uploadedFile }: {
    selectsuggestionFofFile: any,
    suggestions: {
        query: string;
        results: SongType[] | {
            id: string;
            name: string;
            playlist_id: any;
            playlist_name: string;
            artists: any[];
            duration: number;
            likes: number;
            genres: any[];
            album: any;
            thumbnail: string;
            view_count: number;
        }[];
    }, index: number,
    uploadedsong: string,
    uploadedFile: {
        song: any;
        file: File;
    },
    selectedSong: {
        forRow: number;
        selectedIndex: number;
    }[]
}) {
    const { user } = React.useContext(
        AppContext
    ) as AppContextType;
    // console.log("suggestions?.results",suggestions?.results,uploadedFile,"uploadedsong=",uploadedsong)
    const [sugg, setSugg] = React.useState<SongType[] | {
        id: string;
        name: string;
        playlist_id: any;
        playlist_name: string;
        artists: any[];
        duration: number;
        likes: number;
        genres: any[];
        album: any;
        thumbnail: string;
        view_count: number;
    }[]>([])
    const [active, setActive] = React.useState<number>()
    const [searchTerm, setsearchTerm] = React.useState<string>()
    const searchSong = async (newname: string) => {
        if (!user) return
        const song = await findSongByName(user.id, newname);
        setSugg(song)
        setsearchTerm(newname)
    }
    console.log("selectedSong=", selectedSong)
    return (

        <Accordion sx={{ width: '100%', maxHeight: '30' }} className={uploadedFile.file.name ? 'file-present' : 'no-file-present'}>
            <AccordionSummary
                expandIcon={<ExpandMoreIcon />}
                aria-controls="panel1a-content"
                id="panel1a-header"
                sx={{ width: '100%', display: 'grid', gridTemplateColumns: '34fr 1fr' }}
            >

                <Typography variant='subtitle2'>Selected Song Name : {uploadedsong ?? suggestions?.results[0]?.name}    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    Uploaded File Name : {uploadedFile ? uploadedFile.file.name : ''}</Typography>

            </AccordionSummary>
            <AccordionDetails>
                <SearchsongByName searchSong={searchSong} />
                <List>
                    {sugg.length ? sugg.map((x, i) => <ListItem key={'sugg' + x.name + '  ' + i} onClick={() => {
                        selectsuggestionFofFile(x, index, i,searchTerm)
                        setActive(i)
                    }} 
                    sx={active == i ? { color: 'red',justifyContent:'space-between','&:hover':{background:'#6e3c3c'} } : {justifyContent:'space-between','&:hover':{background:'#6e3c3c'} }}>
                    
                        <TextBoxComp song={x} />
                        <MiniPlayer song={x} />
                    </ListItem>) : Array.isArray(suggestions?.results) && suggestions?.results?.map((sugge, j) => <ListItem key={'sugg' + sugge.name + j} onClick={() => {
                        selectsuggestionFofFile(sugge, index, j)
                        setActive(j)
                    }}
                        sx={active == j ? { color: 'red',justifyContent:'space-between','&:hover':{background:'#6e3c3c'} } : {justifyContent:'space-between','&:hover':{background:'#6e3c3c'} }}>
                        <TextBoxComp song={sugge} />
                        <MiniPlayer song={sugge} />
                    </ListItem>)}
                </List>
            </AccordionDetails>
        </Accordion>
    );
}

const MiniPlayer = (song: any) => {
    const [show, setShow] = React.useState<boolean>(false);
    return <Box>
        {/* {show ? <PlayArrow button={()=>setShow(!show)}/>: */}
        <ReactPlayer
            url={song?.song?.preview_url}
            controls={true}
            height={'30px'}
            width={'200px'}
            style={{ color: 'white' }}
        />
        {/* } */}
    </Box>
}

const TextBoxComp = (song: any) => {
    const imgX = song?.song?.album?.images[song?.song?.album?.images.length - 1]
    return <Box sx={{
        display: 'flex',
        minWidth: ' 500px',
        flexDirection: 'row',
        alignItems:'center'
    }}>
        {imgX && <img src={imgX.url} height={imgX.height} width={imgX.width} />}
        <ListItemText primary={song?.song?.name} sx={{fontSize:'small',marginLeft:1}}/>
        <Typography variant='caption'>{song?.song?.album?.release_date}</Typography>
        <Typography variant='caption'>{song?.song?.album?.artists?.map((x: any) => x?.name +' , ')}</Typography>
    </Box>
}
