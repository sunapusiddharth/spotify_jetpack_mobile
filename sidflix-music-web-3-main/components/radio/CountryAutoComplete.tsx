import * as React from 'react';
import TextField from '@mui/material/TextField';
import Autocomplete, { AutocompleteRenderInputParams } from '@mui/material/Autocomplete';

export default function RadioCountries({ genres, setCountry }: {
    genres: {
        name: string;
        count: number;
        code:string
    }[], setCountry: any
}) {
    const onDataChange = (e: any, value: {
        name: string;
        count: number;
        code:string
    } | null) => {
        if (value) setCountry(value.code.toUpperCase())
    }
    return (
        <Autocomplete
            size='small'
            disablePortal
            id="combo-box-demo"
            options={genres}
            getOptionLabel={(option) => `${option.name} (${option.count})`}
            sx={{ width: 300,mt:2 }}
            onChange={onDataChange}
            renderInput={(params: AutocompleteRenderInputParams) => <TextField {...params} label="Countries" />}
        />
    );
}

