import React, { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "react-query";
import { fetchQueueData, getAllAvailableSongs, liekdislikesong } from "../api";
import { AppContext, AppContextType } from "../pages/_app";
import { Box, IconButton, Link, Table, TableBody, TableCell, TableHead, TableRow, Typography } from "@mui/material";
import { checkIfUserHasLikedSong, secondsToHm } from "../utils";
import { History, ArrowBack, ArrowForward, HeatPumpSharp, HeartBroken, Favorite } from '@mui/icons-material'
import Grid2 from "@mui/material/Unstable_Grid2";
import Image from 'next/image'
export const StreamableTracks = () => {
    const [page, setPage] = useState<number>(0)
    const { song, setCurrentSong, user, setUser, openQueue, setopenQueue } = React.useContext(
        AppContext
    ) as AppContextType;
    const queryClient = useQueryClient()
    const {
        data
    } = useQuery(
        ['getAllAvailableSongs', page],
        async () => await getAllAvailableSongs(page * 30, 30),
        {
            keepPreviousData: true,
            refetchOnMount: false,
            refetchOnWindowFocus: false,
        }
    );
    const saveToLikedSongs = useMutation(({ songid, likedislike }: { songid: string, likedislike: boolean }) => liekdislikesong(user?.id || '1', likedislike, songid), {
        onSuccess: (data) => {
            queryClient.setQueryData(['user', { id: user?.id }], data)
            setUser(data)
            console.log("OnSuccess", user?.id, data)
        }
    })

    return <Box mt={2} mb={2} sx={{ display: { xs: 'none', md: 'block' } }}>
        <Grid2 container justifyContent="space-between" gap={2} alignItems="center">
            <Grid2>
                <Typography variant='subtitle2' sx={{ fontVariant: 'all-small-caps' }}>New Tracks Added !!</Typography>
            </Grid2>
            <Grid2 container gap={2} alignItems="center">
                <Grid2>
                    <Typography>Page {page + 1}</Typography>
                </Grid2>
                <Grid2><IconButton onClick={() => setPage(page - 1 || 0)}>
                    <ArrowBack fontSize="small" />
                </IconButton></Grid2>
                <Grid2>
                    <IconButton onClick={() => setPage(page + 1 || 0)}>
                        <ArrowForward fontSize="small" />
                    </IconButton></Grid2>
            </Grid2>
        </Grid2>
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
                {data?.results?.map((x, index) => <TableRow onClick={() => setCurrentSong(x)} key={`new-tracks-streaming-available-${x.id}-${index}`}>
                    <TableCell>{index}</TableCell>
                    <TableCell sx={{ '&:hover': { cursor: 'pointer' } }}>
                        <Image alt="" src={x.thumbnail && x.thumbnail !== 'no-cover.jpg' ? x.thumbnail : "/sample.jpg"}
                            className='imsage' width={60} height={60} />
                    </TableCell>
                    <TableCell sx={{ maxWidth: 400, '&:hover': { cursor: 'pointer' } }}>{x.name}</TableCell>
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
    </Box>
}