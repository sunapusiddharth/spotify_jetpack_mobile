import * as React from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Modal from '@mui/material/Modal';
import { FormControl, FormControlLabel, FormLabel, Radio, RadioGroup, Snackbar, TextField } from '@mui/material';
import { addS3Link, uploadMultipleSongs } from '../../api';
import { AppContext, AppContextType } from '../../pages/_app';
import Dropzone from "react-dropzone";
import { SongType } from '../../types/Song.type';

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

export default function UplaodTrackModal({ open, setOpen, song }: { open: boolean, setOpen: React.Dispatch<React.SetStateAction<boolean>>, song: SongType | undefined }) {
    const { user } = React.useContext(AppContext) as AppContextType
    const [uploadedsongs, setUploadedSongs] = React.useState<{ song: any, file: File }[]>([])
    const [openSnack, setopenSnack] = React.useState<boolean>(false)
    const [message, setMessage] = React.useState<string>('')
    const [option, setOption] = React.useState<string>('update_track')
    const [temps3link, setemps3link] = React.useState<string>(song?.s3link || '')

    const onFilesDrop = async (acceptedFiles: File[]) => {
        if (!user) return;
        // '5 Seconds of Summer - No Shame'.replace(/\.[^/.]+$/, "").replace(/[^a-zA-Z0-9]/g, ' ')
        // x.name.replace(/\.[^/.]+$/, "").replace(/[^a-zA-Z]/g,' ').trim()
        // x.name.replace(/\.[^/.]+$/, "").replace(/[^a-zA-Z0-9]/g, ' ').trim()
        // const fileNames = acceptedFiles.map(x => x.name.replace(/\.[^/.]+$/, "").replace(/[^a-zA-Z]/g,' ').trim())
        setUploadedSongs([{ song: song, file: acceptedFiles[0] }])

    }
    const handleClose = () => setOpen(false);

    const uploadAllSongs = async () => {
        try {
            if (option == 'update_track' && song) {
                const snf = await addS3Link(song.id, temps3link)
                setMessage(`Success in updating track's s3link ${snf.s3link}`)
            } else {
                const keys = await uploadMultipleSongs(uploadedsongs)
                setMessage(`Success in uploading ${keys}`)
            }
            setopenSnack(false)
        } catch (error) {
            setMessage(`Error in uploading ${error}`)
            setopenSnack(true)
        }
    }

    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setOption((event.target as HTMLInputElement).value);
    };
    const handleS3lnkUpdate = (event: React.ChangeEvent<HTMLInputElement>) => {
        setemps3link((event.target as HTMLInputElement).value);
    };


    return (
        <div>
            <Modal
                open={open}
                onClose={handleClose}
                aria-labelledby="modal-modal-title"
                aria-describedby="modal-modal-description"
            >
                <Box sx={style}>
                    <Typography id="modal-modal-title" variant="caption" component="h2">
                        Re-upload song's track
                    </Typography>
                    <Typography id="modal-modal-name" variant="caption" component="h2">
                        {song?.name}, By :{song?.artists[0]?.title}
                    </Typography>
                    <FormControl>
                        <FormLabel id="demo-controlled-radio-buttons-group">Select</FormLabel>
                        <RadioGroup
                            aria-labelledby="demo-controlled-radio-buttons-group"
                            name="controlled-radio-buttons-group"
                            value={option}
                            onChange={handleChange}
                        >
                            <FormControlLabel value="update_track" control={<Radio />} label="update track" />
                            <FormControlLabel value="reupload_track" control={<Radio />} label="upload new track" />
                        </RadioGroup>
                    </FormControl>
                    {option == 'reupload_track' ? <Dropzone onDrop={acceptedFiles => onFilesDrop(acceptedFiles)}>
                        {({ getRootProps, getInputProps }) => (
                            <section>
                                <div {...getRootProps()}>
                                    <input {...getInputProps()} />
                                    <p>Drag n drop some files here, or click to <span className="select-files">select</span> files</p>
                                </div>
                            </section>
                        )}
                    </Dropzone> : <TextField onChange={handleS3lnkUpdate} value={temps3link} />}
                    <Button onClick={uploadAllSongs}>Update Track</Button>
                </Box>
            </Modal>
            <Snackbar
                anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
                open={openSnack}
                onClose={() => setopenSnack(false)}
                message={message}
                key='snackbar'
            />
        </div>
    );
}
