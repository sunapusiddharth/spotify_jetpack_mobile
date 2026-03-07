import * as React from 'react';
import TextField from '@mui/material/TextField';
import Autocomplete, { AutocompleteRenderInputParams } from '@mui/material/Autocomplete';

export default function RadioGenres({ genres, setGenre }: { genres: { genre: string }[], setGenre: any }) {
    const onDataChange = (e: any, value: {
        genre: string;
    } | null) => {
        if (value) setGenre(value.genre)
    }
    // console.log("Allgenres",genres)
    return (
        <Autocomplete
            size='small'
            disablePortal
            id="combo-box-demo"
            options={genres}
            onChange={onDataChange}
            getOptionLabel={(option) => option.genre}
            sx={{ width: 300 }}
            renderInput={(params: AutocompleteRenderInputParams) => <TextField {...params} label="Genres" />}
        />
    );
}

