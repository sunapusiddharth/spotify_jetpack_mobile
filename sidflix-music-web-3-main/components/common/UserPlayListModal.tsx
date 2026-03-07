import { Box, Button, Checkbox, FormControlLabel, FormGroup, Modal, Typography } from "@mui/material";
import * as React from "react";
import { useQuery, useQueryClient } from "react-query";
import { addSongToPlaylist, getUserPlaylist } from "../../api";
import { AppContext, AppContextType } from "../../pages/_app";
import { SongType } from "../../types/Song.type";

const style = {
    position: 'absolute' as 'absolute',
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    width: 400,
    bgcolor: 'background.paper',
    border: '2px solid #000',
    boxShadow: 24,
    p: 2,
};

export const UserPlayLists = (
    { open, setOpen,
        song }: {
            open: boolean, setOpen: React.Dispatch<React.SetStateAction<boolean>>,
            song: SongType | undefined
        }
) => {
    if (!song) return <></>
    const { user } = React.useContext(
        AppContext
    ) as AppContextType;
    const [err, setErr] = React.useState<string>();
    const [checked, setChecked] = React.useState<string[]>();

    const {
        data,
        isLoading,
        isError,
        error,
    } = useQuery(
        ["user_playlist", user?.id],
        async () => await getUserPlaylist(user?.id!),
        {
            keepPreviousData: false,
            refetchOnMount: true,
            refetchOnWindowFocus: true,
        }
    );
    const queryClient = useQueryClient()
    const handleClose = () => {
        setOpen(false)
        setErr('')
        setChecked([])
    };


    const handleCheckBOxChange = (event: React.ChangeEvent<HTMLInputElement>, selectedid: string) => {
        let newChecked = checked || []
        if (!data?.length) return
        if (checked?.includes(selectedid)) {
            //already in user's playlist
            if (!event.currentTarget.checked) {
                //nchecked
                newChecked = newChecked.filter(x => x !== selectedid)
            } else {
                //do nothing
            }
        } else {
            //user has added to his checklist
            newChecked.push(selectedid)
        }
        setChecked(newChecked);
    };

    React.useEffect(() => {
        if (data?.length) {
            const presentinplcs: string[] = []
            data.forEach(x => {
                if (x.tracks.includes(song.id)) { presentinplcs.push(x.id) }
            })
            setChecked([...presentinplcs])
        }
    }, [data?.length])

    const addtoplaylist = async () => {
        console.log("selectedPlayLists", checked)
        if (!user?.id || !checked?.length) return
        await addSongToPlaylist(user.id, checked, song.id, song.thumbnail)
        for (const x of checked) {
            console.log("invalidating playlist for", x)
            await queryClient.invalidateQueries(['playlist', x])
        }
        setOpen(false)
        console.log("Close called")
    }
    return (
        <Modal
            open={open}
            onClose={handleClose}
            aria-labelledby="modal-modal-title"
            aria-describedby="modal-modal-description"
        >
            <Box sx={style}>
                <Typography>Your PlayLists</Typography>
                <FormGroup>
                    {data?.map((playlist, index) => <FormControlLabel control={<Checkbox checked={checked?.includes(playlist.id)} onChange={(e) => handleCheckBOxChange(e, playlist.id)} />} key={playlist.id} label={playlist.name} sx={{ color: 'white' }} />)}
                </FormGroup>
                <Button color='secondary' onClick={addtoplaylist} sx={{ mt: 2 }}>Add To Playlist</Button>
            </Box>
        </Modal>
    );
};

