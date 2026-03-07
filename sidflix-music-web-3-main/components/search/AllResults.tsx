import React from "react"
import { Grid, Typography, Chip, List, ListItem, ListItemText, CardContent, Card, Box, Button, ListItemAvatar } from "@mui/material"
import Image from 'next/image'
import { Favorite, PlayCircleFilled } from "@mui/icons-material"
import { checkIfUserHasLikedSong, getImageUrl, millisToMinutesAndSeconds } from "../../utils"
import { useMutation, useQueryClient } from "react-query"
import { fetchRadio, getSong, liekdislikesong } from "../../api"
import { AppContext, AppContextType } from "../../pages/_app"
import { SearchPageResType, SearchTypeEnum } from "../../types/Song.type"
import { useRouter } from "next/router"
import Slider from "react-slick"

const AllResults = ({ movies }: { movies: SearchPageResType }) => {
    const queryClient = useQueryClient()
    const { setCurrentSong, user, carouseSettings, song: selectedSong, setPodcast, setStation } = React.useContext(
        AppContext
    ) as AppContextType;
    const router = useRouter()
    const artists = movies?.cards?.filter(x => x.type == SearchTypeEnum.artist)
    const albums = movies?.cards.filter(x => x.type == SearchTypeEnum.album)
    const radios = movies?.cards.filter(x => x.type == SearchTypeEnum.radio)
    const podcasts = movies?.cards.filter(x => x.type == SearchTypeEnum.podcast)
    const topsong = getTopSonsgs(movies)
    const saveToLikedSongs = useMutation(({ songid, likedislike }: { songid: string, likedislike: boolean }) => liekdislikesong(user?.id || '1', likedislike, songid), {
        onSuccess: (data) => {
            queryClient.setQueryData(['user', { id: user?.id }], data)
        }
    })

    async function setCurrentSong2(id: string) {
        const sng = await getSong(id)
        setCurrentSong(sng)
    }


    async function onClicHandler(id: string, type: SearchTypeEnum) {
        console.log("called onClicHandler",id,'type',type)
        if (type == SearchTypeEnum.podcast) {
            router.push(`/podcast/${id}`)
        }
        if (type == SearchTypeEnum.artist) {
            router.push(`/artist/${id}`)
        }
        if (type == SearchTypeEnum.album) {
            router.push(`/album/${id}`)
        }
        if (type == SearchTypeEnum.radio) {
            const radio = await fetchRadio(id)
            setStation(radio)
        }
        if (type == SearchTypeEnum.songs) {
            const song = await getSong(id)
            setCurrentSong(song)
        }
    }
    // const [hover, setHover] = React.useState<boolean>(false)
    return (
        <Box mt={2}>
            <Grid container justifyContent={'space-between'} flexWrap='nowrap' columnSpacing={2} mt={2}>
                <Grid item className='artist_background_cover' sx={{
                    '&:hover': {
                        backgroundColor: ' rgb(51, 54, 54)',
                        /* opacity: 0.6; */
                        cursor: 'pointer'
                    }
                }}
                    xs={4}
                // onMouseOver={() => setHover(true)} 
                // onMouseOut={() => setHover(false)}
                >
                    <Typography variant='subtitle1' mb={0.3}>Top result</Typography>
                    <Box display={'flex'} flexDirection='row' justifyContent={'space-between'} alignItems='flex-end'>
                        <Box onClick={() => onClicHandler(topsong.id, topsong.type)} >
                            <Image alt="" src={getImageUrl(topsong.poster) || "/sample.jpg"} width={150} height={150} className='asrtist_avatar' />
                            <Typography width={150} fontSize="x-small" sx={{ textWrap: 'balance' }}>{topsong.title}</Typography>
                            <Box >
                                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                                    <Typography fontSize={'x-small'} sx={{ marginTop: '3%' }}>{topsong.artists}</Typography>
                                    <Chip label={topsong.type} size="small" />
                                </Box>
                            </Box>
                        </Box>
                        {/* {hover && <img src="/play_icon.png" height={70} width={70} />} */}
                    </Box>


                </Grid>
                <Grid item xs={8}>
                    <Typography variant='h6'>Songs</Typography>
                    <List className='all_search_list'>
                        {topsong.songs?.map((song, index) => <ListItem key={'playlist-song-' + index} onClick={() => onClicHandler(song.id, song.type)}
                            className={selectedSong?.id == song.id ? 'selected_song2' : 'playlist_song2'}>
                            <ListItemAvatar>
                            <Image alt="" src={getImageUrl(song.image) || "/sample.jpg"} width={50} height={50} className='asrtist_avatar' />
                            </ListItemAvatar>
                            <ListItemText sx={{
                                width: 500, paddingRight: 3,
                            }}>
                                <Typography sx={{ overflow: "hidden", textOverflow: "ellipsis" }} variant={'caption'} noWrap>{song.name}</Typography>
                            </ListItemText>
                            <ListItemText>
                                <Favorite fontSize="medium" onClick={() => saveToLikedSongs.mutate({ songid: song.id, likedislike: checkIfUserHasLikedSong(user, song.id) })} className='play-comp-icons' />
                            </ListItemText>
                        </ListItem>)}
                    </List>
                </Grid>
            </Grid >
            {
                [
                    { label: 'Artists', data: artists }, { label: 'Albums', data: albums }, { label: 'Radio', data: radios },
                    { label: 'Podcasts', data: podcasts }
                ].map((x, index2) => x.data?.length ?
                    <Box key={'search-carousels' + index2 + x.label} sx={{}}>
                        <Grid container justifyContent={'space-between'} >
                            <Grid item sx={{ marginBottom: 1 }}>
                                <Typography variant='h5' sx={{ marginBottom: '2%' }}>{x.label}</Typography>
                            </Grid>
                            <Grid item>
                                <Button >
                                    See All
                                </Button>
                            </Grid>
                        </Grid>
                        <Box className='container'>
                            <Slider {...carouseSettings}>
                                {x.data?.map((album, index) => <Card sx={{ width: 180, height: 240, background: 'black', '&:hover': { cursor: 'pointer' } }} onClick={() => onClicHandler(album.id, album.type)}>
                                    <Image alt="" src={album.image && album.image !== 'no-cover.jpg' ? getImageUrl(album.image) : "/sample.jpg"} width={180} height={200} />
                                    <CardContent sx={{ fontSize: 'x-small', padding: 1 }}>
                                        <Typography component="div" sx={{ fontSize: 'x-small' }} textAlign={'center'}>{album?.name?.length > 30 ? album?.name?.slice(0, 30) + '...' : album?.name}</Typography>
                                        {/* <Chip label={album.type} sx={{}} size="small" /> */}
                                    </CardContent>
                                </Card>)}
                            </Slider>
                        </Box>

                    </Box> : <></>)
            }
        </Box>

    )
}

const getTopSonsgs = (results: SearchPageResType) => {
    const top = results.cards[0]

    return {
        poster: top.image,
        title: top.name,
        artists: top.artist,
        type: top.type,
        id: top.id,
        songs: results.cards?.filter(x => x.type == SearchTypeEnum.songs).slice(0, 4)
    }
}

export default AllResults

