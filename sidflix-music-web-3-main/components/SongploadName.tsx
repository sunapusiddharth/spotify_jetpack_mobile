import * as React from 'react';
import Paper from '@mui/material/Paper';
import InputBase from '@mui/material/InputBase';
import Divider from '@mui/material/Divider';
import IconButton from '@mui/material/IconButton';
import MenuIcon from '@mui/icons-material/Menu';
import SearchIcon from '@mui/icons-material/Search';
import DirectionsIcon from '@mui/icons-material/Directions';
import { TextField } from '@mui/material';

export default function SearchsongByName({  searchSong}: {searchSong: any }) {
    const [newname, setName] = React.useState<string>();
    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setName(event.target.value);
    };
    return (
        <>
            {/* <InputBase
                sx={{ ml: 1, flex: 1 }}
                placeholder={name}
                inputProps={{ 'aria-label': 'search google maps' }}
            /> */}
            <TextField
                id="outlined-name"
                label="Name"
                value={newname}
                onChange={handleChange}
            />
            <IconButton sx={{ p: '10px' }} aria-label="search">
                <SearchIcon onClick={() => searchSong(newname)} />
            </IconButton>
        </>
    );
}
