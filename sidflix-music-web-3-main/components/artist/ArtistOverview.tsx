import { Addchart, Favorite, History } from "@mui/icons-material";
import { Box, Grid, IconButton, List, ListItem, ListItemText, Table, TableBody, TableCell, TableHead, TableRow, Typography } from "@mui/material";
import Link from "next/link";
import * as React from "react";
import { useMutation, useQueryClient } from "react-query";
import { liekdislikesong } from "../../api";
import { AppContext, AppContextType } from "../../pages/_app";
import { ArtistType } from "../../types/ArtistType";
import { SongType } from "../../types/Song.type";
import { checkIfUserHasLikedSong, millisToMinutesAndSeconds, secondsToHm } from "../../utils";
import { ContentCard } from "../home/Card";
import Slider from "react-slick";
import { UserPlayLists } from "../common/UserPlayListModal";
import Image from 'next/image'
interface COompProps {
    artist: ArtistType
}

const ArtistOverview: React.FC<COompProps> = (
    props: COompProps
) => {
    const { artist } = props
    const queryClient = useQueryClient()
    const { setCurrentSong, user, setUser, carouseSettings: sliderSettings } = React.useContext(
        AppContext
    ) as AppContextType;
    const [expanded, setExpanded] = React.useState<boolean>(false);
    const [songs, setSongs] = React.useState<SongType[]>(artist.popular_songs);
    const [selectedSong, setSelectedSong] = React.useState('');
    const saveToLikedSongs = useMutation(({ songid, likedislike }: { songid: string, likedislike: boolean }) => liekdislikesong(user?.id || '1', likedislike, songid), {
        onSuccess: (data) => {
            queryClient.setQueryData(['user', { id: user?.id }], data)
            //also update the context data , if we were using useWuery on 'user it would automatically update 
            setUser(data)
            console.log("OnSuccess", user?.id, data)
        }
    })
    const handleSelectSong = (song: SongType) => {
        //console.log("IN handleSelectSong",song)
        setSelectedSong(song.id);
        setCurrentSong(song)
    }
    // React.useEffect(() => {
    //     setSongs(data?.slice(0, 6) || [])
    // }, [data?.length])
    const [addToPlayListSong, setaddToPlayListSong] = React.useState<SongType>();
    const [open, setOpen] = React.useState<boolean>(false);
    const hadleAddtoplaylist = (song: SongType) => {
        setaddToPlayListSong(song)
        setOpen(true)
    }
    // const handleChange = () => {
    //     const updatedData = data?.concat(data?.slice(5, data.length - 1) || [])
    //     setSongs(updatedData || [])
    //     setExpanded(!expanded)
    // };



    return (
        <>
            <Typography variant='h6' marginTop={4}>Top Tracks</Typography>
            <Table >
                <TableHead>
                    <TableRow>
                        <TableCell>#</TableCell>
                        <TableCell></TableCell>
                        <TableCell>Title</TableCell>
                        {/* <TableCell>Artist</TableCell>
                    <TableCell>Album</TableCell> */}
                        <TableCell>
                            <Image src="/love.png" alt={"love"} width={20} height={20} />

                        </TableCell>
                        <TableCell><History fontSize="small" /></TableCell>
                    </TableRow>

                </TableHead>
                <TableBody> 
                    {songs?.map((x, index) => <TableRow onClick={() => setCurrentSong(x)} key={`new-tracks-streaming-available-${x.id}-${index}`}>
                        <TableCell>{index}</TableCell>
                        <TableCell sx={{ '&:hover': { cursor: 'pointer' } }}>
                            <Image alt="" src={x.thumbnail && x.thumbnail !== 'no-cover.jpg' ? x.thumbnail : "/sample.jpg"}
                                className='imsage' width={60} height={60} 
                                style={{ filter: `${x.s3link ? 'grayscale(0%)' : 'grayscale(90%)'}` }}/>
                        </TableCell>
                        <TableCell sx={{ maxWidth: 400, '&:hover': { cursor: 'pointer' } }}>
                            {x.name}
                            </TableCell>
                        {/* <TableCell>{x.artists?.map(t => <Link href={'/artist' + t.id} key={'new-tracks-streaming-available_artist-' + t.id + x.id}>{t.title}</Link>)}</TableCell> */}
                        {/* <TableCell>{x.album?.title}</TableCell> */}
                        <TableCell>
                            {!checkIfUserHasLikedSong(user, x.id) ?
                                <Image src="/love.png" alt={"love"} width={20} height={20} /> :
                                <IconButton onClick={() => saveToLikedSongs.mutate({ songid: x.id, likedislike: checkIfUserHasLikedSong(user, x.id) })} >
                                    <Favorite fontSize="small" />
                                </IconButton>}
                        </TableCell>
                        <TableCell>{x.duration ? secondsToHm(x.duration / 1000) : ''}</TableCell>
                    </TableRow>)}
                </TableBody>
            </Table>
            {artist.singles?.cards?.length && <Box>
                <Typography variant='h6'>Singles</Typography>
                <Grid>
                    {artist.singles.cards.map(x => <ContentCard data={
                        {
                            image: x.image,
                            id: x.id,
                            title: x.title,
                            subtitle: '',
                            type: 'music_card',
                            path: '',
                            song: x.song
                        }
                    } />)}
                </Grid>
            </Box>}
            {artist.featured_albums?.cards?.length && <Box>
                <Typography variant='h6'>Featured Albums</Typography>
                <Slider {...sliderSettings}>
                    {artist.featured_albums.cards.map(x => <ContentCard data={
                        {
                            image: x.image,
                            id: x.id,
                            title: x.title,
                            subtitle: '',
                            type: 'music_card',
                            path: '',
                            song: x.song
                        }
                    } />)}
                </Slider>
            </Box>}
            {artist.fans_also_like?.cards?.length && <Box>
                <Typography variant='h6'>Fans Also Like</Typography>
                <Slider {...sliderSettings}>
                    {artist.fans_also_like.cards.map(x => <ContentCard data={
                        {
                            image: x.image,
                            id: x.id,
                            title: x.title,
                            subtitle: '',
                            type: 'music_card',
                            path: '',
                            song: x.song
                        }
                    } />)}
                </Slider>
            </Box>}
            {artist.dicovered_on?.cards?.length && <Box>
                <Typography variant='h6'>Discovered on</Typography>
                <Slider {...sliderSettings}>
                    {artist.dicovered_on.cards.map(x => <ContentCard data={
                        {
                            image: x.image,
                            id: x.id,
                            title: x.title,
                            subtitle: '',
                            type: 'music_card',
                            path: '',
                            song: x.song
                        }
                    } />)}
                </Slider>
            </Box>}
            {artist.albums_featuring_artist?.cards?.length && <Box>
                <Typography variant='h6'>Albums Featuring Artist</Typography>
                <Slider {...sliderSettings}>
                    {artist.albums_featuring_artist.cards.map(x => <ContentCard data={
                        {
                            image: x.image,
                            id: x.id,
                            title: x.title,
                            subtitle: '',
                            type: 'music_card',
                            path: '',
                            song: x.song
                        }
                    } />)}
                </Slider>
            </Box>}

            <UserPlayLists open={open} setOpen={setOpen} song={addToPlayListSong} />
        </>
    );
};

export default ArtistOverview;
