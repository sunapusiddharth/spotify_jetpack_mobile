import react, * as React from "react";
import { Grid, IconButton, NoSsr, Tooltip } from '@mui/material'
import ReactPlayer from 'react-player'
import { PlaylistAdd, QueueMusic, Favorite } from "@mui/icons-material";
import { AppContext, AppContextType } from "../pages/_app";
import Router from "next/router";
import { getNextQueueItem, getPrevQueueItem, getStreamLink, liekdislikesong, requestTrackAddition, userListenedPodcasts, userListenedStation } from "../api";
import { useMutation, useQueryClient } from "react-query";
import useWindowDimensions from "../hooks/windowDimensio";
import { CoverImg } from "./player/CoverImage";
import { Buttons } from "./player/Buttons";
import { SeekBar } from "./player/SeekBar";
import { UserPlayLists } from "./common/UserPlayListModal";
import { checkIfUserHasLikedPodcast, checkIfUserHasLikedRadio, checkIfUserHasLikedSong } from "../utils";
import { SongType } from "../types/Song.type";
import { RadioStation } from "../types/RadioStationType";
import { PodcastEpisodeDto } from "../types/PodcastEpisode.dto";

interface CompProps {
    drawerWidth: number
}
export enum MediaType {
    song = 'song',
    radio = 'radio',
    podcast = 'podcast'
}
function Comp(props: CompProps) {
    const { height, width } = useWindowDimensions();
    const isMobile = width < 600 ? true : false
    const { song, user, setCurrentSong, station, stations, setStation,
        setPodcast, podcast, podcastEpisodes, bgColor, setopenQueue, setUser } = React.useContext(
            AppContext
        ) as AppContextType;
    const [playing, setPlaying] = react.useState<boolean>(false)
    const [currentMediaType, setCurrentMediaType] = react.useState<MediaType>()
    const [streamLink, setStreamLink] = react.useState<string>('');
    const [showSpinner, setShowSpinner] = react.useState<boolean>(false)
    const [progress, setupdateProgress] = react.useState<number>(0)
    const [buffered, setBuffered] = react.useState<number>(0)
    const [duration, setDuration] = react.useState<number>(0)
    const [playBackError, setPlayBackError] = react.useState<string>()

    const queryClient = useQueryClient()
    const playerRef = react.createRef<ReactPlayer>();
    React.useEffect(() => {

        if (user && song) {
            getStreamLink(song.s3link, user.id || 'test', song.id).then(res => {
                console.log("calling api", song.s3link, "res=", res)
                setCurrentMediaType(MediaType.song)
                if (res) {
                    setStreamLink(res)
                    setPlaying(true)
                }
            })
        }
    }, [song])
    React.useEffect(() => {
        if (user && !song) {
            getNextQueueItem(user.id).then(song => {
                setCurrentSong(song.song)
                queryClient.setQueryData(['queue', user.id], song.updated_queue)
            })
        }
    }, [])

    React.useEffect(() => {
        //add to user played tracks
        if (user && station) {
            setCurrentMediaType(MediaType.radio)
            userListenedStation(user?.id, station?.id, 0)
            setPlaying(true)
        }
    }, [station])

    React.useEffect(() => {
        //add to user played tracks
        if (user && podcast) {
            setCurrentMediaType(MediaType.podcast)
            setPlaying(true)
            // userListenedStation(user?.id, station?.id, 0)
        }
    }, [podcast])

    const navigateToPlaylist = () => {
        Router.push(
            {
                pathname: "/queue",
            },
            undefined,
            { shallow: true }
        );
    }
    const updateWithNewSong = async (userid: string) => {
        const next_song = await getNextQueueItem(userid)
        setCurrentSong(next_song.song)
        queryClient.setQueryData(['queue', userid], next_song.updated_queue)
        return next_song.song.preview_url
    }
    const onSongEnded = async () => {
        //fetch from queue and setSong
        if (!user) return
        try {
            const preview_url = await updateWithNewSong(user.id)
        } catch (error) {
            console.error("Error in getting next item playing the same song again")
            if (song) setCurrentSong(song)
        }
    }
    const playNextSong = async () => {
        //fetch from queue and setSong
        if (!user) return
        try {
            const preview_url = await updateWithNewSong(user.id)
        } catch (error) {
            console.error("Error in getting next item playing the same song again")
            if (song) setCurrentSong(song)
        }
    }

    const playPrevSong = async () => {
        //fetch from queue and setSong
        if (!user) return
        try {
            const next_song = await getPrevQueueItem(user.id)
            setCurrentSong(next_song.song)
            queryClient.setQueryData(['queue', user.id], next_song.updated_queue)
            //remove from user
        } catch (error) {
            console.error("Error in getting next item playing the same song again")
            if (song) setCurrentSong(song)
        }
    }

    const playNextRadioStation = async () => {
        //fetch from queue and setSong
        if (!stations?.length || !station) return
        try {
            const currentStationIndex = stations?.findIndex(x => x.id == station.id)
            let next_st = stations[0]
            if (currentStationIndex != -1) {
                if (currentStationIndex == stations.length) next_st = stations[0]
                else next_st = stations[currentStationIndex + 1]
            }
            setStation(next_st)
        } catch (error) {
            console.error("Error in getting next item playing the same song again")
        }
    }

    const playPrevRadioStation = async () => {
        //fetch from queue and setSong
        if (!stations?.length || !station) return
        try {
            const currentStationIndex = stations?.findIndex(x => x.id == station.id)
            let prev_st = stations[0]
            if (currentStationIndex != -1) {
                if (currentStationIndex == 0) prev_st = stations[stations.length - 1]
                else prev_st = stations[currentStationIndex - 1]
            }
            setStation(prev_st)
        } catch (error) {
            console.error("Error in getting next item playing the same song again")
        }
    }

    const playNextPodcast = async () => {
        //fetch from queue and setSong
        if (!podcastEpisodes?.length || !podcast) return
        try {
            const currentStationIndex = podcastEpisodes?.findIndex(x => x.uuid == podcast.episode?.uuid)
            let next_st = podcastEpisodes[0]
            if (currentStationIndex != -1) {
                if (currentStationIndex == podcastEpisodes.length) next_st = podcastEpisodes[0]
                else next_st = podcastEpisodes[currentStationIndex + 1]
            }
            setPodcast({ episode: next_st, podcast: podcast.podcast })

        } catch (error) {
            console.error("Error in getting next item playing the same song again")
        }
    }
    function handleNext() {
        if (currentMediaType == 'song') playNextSong()
        if (currentMediaType == 'radio') playNextRadioStation()
        if (currentMediaType == 'podcast') playNextPodcast()
    }

    const playPrevPodcast = async () => {
        //fetch from queue and setSong
        if (!podcastEpisodes?.length || !podcast) return
        try {
            const currentStationIndex = podcastEpisodes?.findIndex(x => x.uuid == podcast.episode?.uuid)
            let prev_st = podcastEpisodes[0]
            if (currentStationIndex != -1) {
                if (currentStationIndex == 0) prev_st = podcastEpisodes[podcastEpisodes.length - 1]
                else prev_st = podcastEpisodes[currentStationIndex - 1]
            }
            setPodcast({ episode: prev_st, podcast: podcast.podcast })
        } catch (error) {
            console.error("Error in getting next item playing the same song again")
        }
    }
    function handlePrev() {
        station ? playPrevRadioStation() : podcast ? playPrevPodcast() : playPrevSong()
    }

    const [open, setOpen] = React.useState<boolean>(false);
    const requestTrack = async () => {
        if (!user || !song) return null;
        await requestTrackAddition(user.id, song?.id)
    }
    const updateProgress = async (event: {
        played: number
        playedSeconds: number
        loaded: number
        loadedSeconds: number
    }) => {
        if (playBackError) {
            setPlayBackError(undefined)
            // handleNext()
        }
        setBuffered(event.loadedSeconds)
        setupdateProgress(event.playedSeconds)
        //todo track 
        if (event.playedSeconds % 30 == 0) {
            if (currentMediaType == 'podcast') {
                await userListenedPodcasts(user?.id!, podcast?.podcast.uuid!, event.playedSeconds, podcast?.episode?.uuid!)
            }
        }
    }
    const signature = streamLink ? new URL(streamLink).searchParams.get('Signature') : ''
    const policy = streamLink ? new URL(streamLink).searchParams.get('Policy') : ''
    // console.log("streamLink && currentMediaType", streamLink, currentMediaType)
    async function likeSong() {
        if (song)
            saveToLikedSongs.mutate({ songid: song.id, likedislike: checkIfUserHasLikedSong(user, song.id) })

    }
    const saveToLikedSongs = useMutation(({ songid, likedislike }: { songid: string, likedislike: boolean }) => liekdislikesong(user?.id || '1', likedislike, songid), {
        onSuccess: (data) => {
            queryClient.setQueryData(['user', { id: user?.id }], data)
            setUser(data)
            console.log("OnSuccess", user?.id, data)
        }
    })

    const LoveTrack = ({ song }: { song: SongType | RadioStation | PodcastEpisodeDto | undefined }) => {

        return <></>

    }

    return <NoSsr >
        <footer className='song_player_comp' style={{
            // backgroundColor: 'rgb(22 23 24)',
            // background: 'linear-gradient(0deg, #1c0650 0%, #4b0070 100%) !important',

            backgroundImage: 'linear-gradient(0deg, #1c0650 0%, #4b0070 100%)'

        }}>
            <Grid container className='button_metadaga_container' style={{ height: '100%', minHeight: 63 }} >
                <Grid item sx={{ display: 'flex', alignItems: 'center' }}  >
                    <CoverImg song={currentMediaType == 'song' ? song : currentMediaType == 'radio' ? station : currentMediaType == 'podcast' ? podcast?.episode : song} currentMediaType={currentMediaType} />
                    <Grid item sx={{ textAlign: 'center', width: '70%' }}>
                        <Buttons playing={playing} handlePrev={handlePrev} handleNext={handleNext} setPlaying={setPlaying} />
                    </Grid>
                </Grid>
                <Grid >
                    <div style={{ display: 'flex' }}>

                        {/* <ProgressBar progress={progress} setProgress={setupdateProgress} buffer={buffered} playNext={handleNext} setBuffer={setBuffered} duration={duration} /> */}
                        <SeekBar progress={progress} playBackError={playBackError} duration={duration} playerRef={playerRef} buffer={buffered} />
                        {/* <MusicPlayerSlider progress={progress} setProgress={setupdateProgress} buffer={buffered} playNext={handleNext} setBuffer={setBuffered} duration={duration} playing={playing} song={song} handlePrev={handlePrev} handleNext={handleNext} setPlaying={setPlaying} playerRef={playerRef} /> */}
                        {
                            streamLink && currentMediaType == 'song' ? <ReactPlayer
                                url={streamLink}
                                controls={false}
                                playing={playing}
                                playsinline={true}
                                height={'30px'}
                                width={'77vh%'}
                                ref={playerRef}
                                forceAudio={true}
                                style={{ color: 'white' }}
                                onPlay={() => setShowSpinner(false)}
                                onBuffer={() => setShowSpinner(true)}
                                config={{
                                    file: {
                                        forceHLS: true,
                                        hlsOptions: {
                                            xhrSetup: function (xhr: any, url: string) {
                                                if (url.indexOf('https://dpashj04akcoe.cloudfront.net/') === 0) {
                                                    if (url.slice(-2) == 'ts') {
                                                        url += '?' + `Policy=${policy}&Key-Pair-Id=K3QOPS3KJYTLPK&Signature=${signature}`; // this suffix is updated every [x] seconds by polling the server
                                                        xhr.open('GET', url, true);
                                                    }
                                                }
                                            },
                                        }
                                    }
                                }}
                                onEnded={onSongEnded}
                                onProgress={updateProgress}
                                onDuration={(e) => setDuration(e)}
                                onError={(e) => setPlayBackError(e)}

                            /> : <ReactPlayer
                                url={currentMediaType == 'radio' ? station?.url : currentMediaType == 'song' ? song?.preview_url : currentMediaType == 'podcast' ? podcast?.episode?.audio_url : ''}
                                controls={false}
                                playing={playing}
                                height={'30px'}
                                width={'77vh%'}
                                ref={playerRef}
                                style={{ color: 'white' }}
                                onEnded={onSongEnded}
                                onPlay={() => setShowSpinner(false)}
                                onBuffer={() => setShowSpinner(true)}
                                onProgress={updateProgress}
                                onDuration={(e) => setDuration(e)}
                            />}

                    </div>
                </Grid>
                <Grid item sx={{ maxHeight: '8vh', marginLeft: '5%' }} >
                    <Grid container columnGap='5%' alignItems={"center"}>
                        {!station ? <Tooltip title="Add to Playlist">
                            <IconButton onClick={() => setOpen(true)}>
                                <PlaylistAdd fontSize="medium" style={{ fill: 'white' }} className='play-comp-icons' />
                            </IconButton>
                        </Tooltip> : ''}
                        {!station ? <Tooltip title="Queue">
                            <IconButton onClick={() => setopenQueue(true)}>
                                <QueueMusic fontSize="medium" style={{ fill: 'white' }} className='play-comp-icons' />
                            </IconButton>
                        </Tooltip> : ''}
                        {song ? <Tooltip title="Fav">
                            <>
                                {currentMediaType == 'song' ? <IconButton onClick={() => saveToLikedSongs.mutate({ songid: song.id, likedislike: checkIfUserHasLikedSong(user, song.id) })} >
                                    <Favorite fontSize="small" className={!checkIfUserHasLikedSong(user, song.id) ? 'user-liked-song' : 'play-comp-icons'} />
                                </IconButton> : currentMediaType == 'radio' ?
                                    <IconButton onClick={() => saveToLikedSongs.mutate({ songid: song.id, likedislike: checkIfUserHasLikedRadio(user, song.id) })} >
                                        <Favorite fontSize="small" className={!checkIfUserHasLikedRadio(user, song.id) ? 'user-liked-song' : 'play-comp-icons'} />
                                    </IconButton> : currentMediaType == 'podcast' ? <IconButton onClick={() => saveToLikedSongs.mutate({ songid: song.uuid, likedislike: checkIfUserHasLikedPodcast(user, song.uuid) })} >
                                        <Favorite fontSize="small" className={!checkIfUserHasLikedPodcast(user, song.uuid) ? 'user-liked-song' : 'play-comp-icons'} />
                                    </IconButton> : <></>
                                }
                            </>
                        </Tooltip> : ''}

                        {/* {!station ? <Tooltip title="Request Track">
                            <IconButton onClick={() => requestTrack()}>
                                <LibraryAdd style={{ fill: 'white' }} className='play-comp-icons' />
                            </IconButton>
                        </Tooltip> : ''} */}
                        {/* {!station ? <Tooltip title="Lyrics">
                            <IconButton >
                                <Lyrics style={{ fill: 'white' }} className='play-comp-icons' />
                            </IconButton>
                        </Tooltip> : ''} */}

                    </Grid>

                </Grid>
            </Grid>
        </footer>
        <div>
            <UserPlayLists open={open} setOpen={setOpen} song={song} />

        </div>
    </NoSsr>
}

export const Songcomponent = React.memo(
    Comp,
    // moviePropsAreEqual
);