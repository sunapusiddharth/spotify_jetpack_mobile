import { Backdrop, Box, Button, ButtonGroup, Checkbox, CircularProgress, IconButton, Pagination, Snackbar, Stack, Tooltip, Typography } from "@mui/material";
import { ChangeEvent, useContext, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "react-query"
import Image from 'next/image'
import { getAllSongs, deleteSong } from "../api";
import { SongType } from "../types/Song.type";
import { AppContext, AppContextType } from "./_app";
import { Delete, DeleteForever, PlayArrow, SelectAll } from "@mui/icons-material";
import { useTheme } from "@emotion/react";

const EditTracks = () => {
    const [page, setPage] = useState<number>(1);
    const [selecedRows, setSelecedRows] = useState<string[]>([]);
    const [selectedSong, setselectedSong] = useState<string | undefined>();
    const { setCurrentSong } = useContext(AppContext) as AppContextType
    const [openSnack, setopenSnack] = useState<boolean>(false)
    const [openBackdrop, setopenBackdrop] = useState<boolean>(false)
    const [message, setMessage] = useState<string>('')
    const queryClient = useQueryClient()
    const theme = useTheme()
    const {
        isLoading,
        isError,
        error,
        data,
        isFetching,
    } = useQuery(['all_songs', page], () => getAllSongs(page), { keepPreviousData: true });

    async function deleteSelected() {
        console.log("selecedRows", selecedRows)
        setopenBackdrop(true)
        for (const id of selecedRows) {
            await deleteSongFn.mutate({ songid: id })
        }
        handleClose('Selected Songs Deleted')
        setopenBackdrop(false)
    }

    function handlePageChange(value: number) {
        setPage(value)
        //reset
        setSelecedRows([])
        setselectedSong(undefined)

    }
    async function deleteSongFunc(id: string) {
        if (id) {
            setopenBackdrop(true)
            await deleteSongFn.mutate({ songid: id })
            handleClose('Song Deleted')
            setopenBackdrop(false)
        }
    }
    const deleteSongFn = useMutation(({ songid }: { songid: string }) => deleteSong(songid), {
        onSuccess: (data) => {
            const res: any[] | undefined = queryClient.getQueryData(['all_songs', page])
            console.log("fromdel", data)
            if (!res) return undefined
            const newres = res.filter((x: any) => x.id !== data)
            queryClient.setQueryData(['all_songs', page], newres)
        }
    })

    function playSong(s: SongType) {
        setselectedSong(s.id)
        setCurrentSong(s)
    }

    function handlechecBoxSelect(chekced: boolean, songid: string) {
        if (chekced) {
            let x = [...selecedRows, songid]
            console.log("handlechecBoxSelect if", x)
            setSelecedRows(x)
        } else {
            let rows = selecedRows
            let x = [...rows.filter(x => x != songid)]
            console.log("handlechecBoxSelect else", x)
            setSelecedRows([...rows.filter(x => x != songid)])
        }
    }


    const handleClose = (message?: string) => {
        if (message) {
            setMessage(message)
            setopenSnack(true)
        }
    };

    function selectAllRows() {
        if (data?.length) {
            setSelecedRows([...data.map(x => x.id)])
        }
    }

    return <Box>
        <Backdrop
            sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
            open={openBackdrop}
            onClick={() => setopenBackdrop(false)}
        >
            <CircularProgress color="inherit" />
        </Backdrop>
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
            <Typography variant='h6' >Edit Tracks</Typography>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Tooltip title="delete selected"><IconButton onClick={deleteSelected}><Delete /></IconButton></Tooltip>
                <Tooltip title="select all rows"><SelectAll onClick={selectAllRows} /></Tooltip>
            </div>
        </div>

        <div style={{ maxHeight: '78vh', overflow: 'scroll' }}>
            <table style={{ marginTop: 2, }} className="css-edit-track-table">
                <tr className="sticky-header">
                    <th style={{ maxWidth: 100, color: 'white' }}  ></th>
                    <th style={{ width: 100, color: 'white' }} >{'Image'}</th>
                    <th style={{ width: 100, color: 'white' }} >{'Title'}</th>
                    <th style={{ width: 100, color: 'white' }} >{'Album'}</th>
                    <th style={{ width: 100, color: 'white' }} >{'Artist'}</th>
                    <th style={{ width: 100, color: 'white' }} >{'Preview url'}</th>
                    <th style={{ width: 100, color: 'white' }} >{'S3link'}</th>
                    <th style={{ width: 100, color: 'white' }} >{'Play'}</th>
                    <th style={{ width: 100, color: 'white' }} >{'Delete'}</th>
                    <th style={{ width: 100, color: 'white' }} >{'Edit s3link'}</th>
                </tr>
                <tbody>
                    {data?.map((song, index) => <tr key={'edit-song-' + index} className={selectedSong == song.id ? 'selescted_song' : ''}>
                        <td>
                            <Checkbox
                                onChange={(e: ChangeEvent<HTMLInputElement>) => handlechecBoxSelect(e.target.checked, song.id)}
                                edge="start"
                                checked={selecedRows.includes(song.id)}
                                tabIndex={-1}
                                disableRipple
                                inputProps={{ 'aria-labelledby': song.id }}
                            />
                        </td>
                        <td>
                            <Image alt="" src={song.thumbnail && song.thumbnail !== 'no-cover.jpg' ? song.thumbnail : "/sample.jpg"} className='imsage' width={50} height={50} />
                        </td>
                        <td style={{ fontSize: 'small', width: 100 }} id={'song-image'} >{song.name}</td>
                        <td style={{ fontSize: 'small', width: 100 }} id={'song-album'} >{song.album_name}</td>
                        <td style={{ fontSize: 'small', width: 100 }} id={'song-artist'} >{song.artist_name}</td>
                        <td style={{ fontSize: 'small', width: 70 }} id={'song-previewUrl'}  >{song.preview_url}</td>
                        <td style={{ fontSize: 'small', width: 70 }} id={'song-s3link'} >{song.s3link} </td>
                        <td style={{ fontSize: 'small', width: 20 }} >
                            <IconButton onClick={() => playSong(song)}>
                                {song.s3link ? <PlayArrow /> : <></>}                        </IconButton>
                        </td>
                        <td title="delete" style={{ fontSize: 'small', width: 20 }}>
                            <IconButton onClick={() => deleteSongFunc(song.id)}>
                                <DeleteForever />                       </IconButton>
                        </td>
                        <td title="edit s3link" style={{ fontSize: 'small', width: 20 }} />
                    </tr>)}
                </tbody>
            </table>
        </div>

        <Stack  sx={{display:'flex',alignItems:'center'}}>
            <Pagination variant="outlined" shape="rounded" count={10} page={page} onChange={(event: React.ChangeEvent<unknown>, value: number) => handlePageChange(value)} />
        </Stack>
        <Snackbar
            anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
            open={openSnack}
            onClose={() => setopenSnack(false)}
            message={message}
            key='snackbar'
            autoHideDuration={1500}
        />
    </Box>
}


export default EditTracks