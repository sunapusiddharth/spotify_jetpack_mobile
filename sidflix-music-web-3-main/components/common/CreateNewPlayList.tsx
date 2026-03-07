import * as React from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Modal from '@mui/material/Modal';
import { Snackbar, TextField } from '@mui/material';
import { createNewPlaylist, uploadImage } from '../../api';
import { AppContext, AppContextType } from '../../pages/_app';
import { useQueryClient } from 'react-query';
import { editor_user_id } from './SideBar';


export default function CreateNewPlayListModal({ open, setOpen }: { open: boolean, setOpen: React.Dispatch<React.SetStateAction<boolean>> }) {
    const { user } = React.useContext(AppContext) as AppContextType
    const [name, setName] = React.useState<string>();
    const [image, setImage] = React.useState({ preview: '', data: '' })
    const [status, setStatus] = React.useState('')
    const [Imgopen, setImgOpen] = React.useState(false);

    const style = {
        position: 'absolute' as 'absolute',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        width: editor_user_id == user?.id ? '60%' : '30%',
        height: editor_user_id == user?.id ? '41%' : '30%',
        bgcolor: 'background.paper',
        border: '2px solid #000',
        boxShadow: 24,
        p: 2,
    };

    const handleImgClose = (event?: React.SyntheticEvent | Event, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setImgOpen(false);
    };

    // const handleSubmit = async (e) => {
    //     e.preventDefault()
    //     let formData = new FormData()
    //     formData.append('file', image.data)
    //     const response = await fetch('http://localhost:5000/image', {
    //         method: 'POST',
    //         body: formData,
    //     })
    //     if (response) setStatus(response.statusText)
    // }

    const handleFileChange = async (e: any) => {
        const img = {
            preview: URL.createObjectURL(e.target.files[0]),
            data: e.target.files[0],
        }
        setImage(img)
        try {

            setImgOpen(true)
            setStatus('Image upload success')
        } catch (error) {
            setImgOpen(true)
            setStatus(`Image upload error ${JSON.stringify(error)}`)
            console.error("Error in adding playlist!!!!!", error)
        }
    }


    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setName(event.target.value);
    };

    const [err, setErr] = React.useState<string>();
    const queryClient = useQueryClient()
    const handleClose = () => setOpen(false);

    const createNewPlayListHandler = async () => {
        try {
            if (!user) {
                setErr('Error in creating new playlist')
                return
            }
            //@ts-ignore
            const filename = await uploadImage(image.data)
            await createNewPlaylist(user?.id, name || '', filename)
            await queryClient.invalidateQueries(['user', user.id])
            await queryClient.invalidateQueries(['user_playlist', user.id])
            setErr('')
            setOpen(false);
        } catch (error) {
            setErr('Error in creating new playlist')
        }
    }
    const bg = image.preview ? ` url(${image.preview})` : ''
    return (
        <div>
            <Modal
                open={open}
                onClose={handleClose}
                aria-labelledby="modal-modal-title"
                aria-describedby="modal-modal-description"
            >
                <Box sx={{ ...style, backgroundImage: bg, backgroundSize: 'cover' }} >
                    <Typography id="modal-modal-title" variant="caption" component="h2" sx={{color:'whitesmoke'}}>
                        Add New Playlist
                    </Typography>
                    <Box sx={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between', height: '100%' }}>
                        <TextField
                            id="outlined-name"
                            label="your playlist name"
                            value={name}
                            onChange={handleChange}

                            sx={{ mt: 2 }}
                        />
                        <div className="file-input">
                            <input type="file" id="file" className="file" onChange={handleFileChange} />
                            <label htmlFor="file">Select file</label>
                        </div>
                        <Button color='secondary' sx={{ color: 'white', maxWidth: 100 }} size="small" variant="contained" onClick={createNewPlayListHandler}>Add</Button>
                        {err && <Typography>Something went wrong please try again..</Typography>}
                        <Box>
                        </Box>
                    </Box>
                </Box>
            </Modal>
            <Snackbar
                anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
                open={Imgopen}
                onClose={handleImgClose}
                message={status}
                key='snackbar'
            />
        </div >
    );
}
