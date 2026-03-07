import styled from '@emotion/styled';
import { SearchSharp } from '@mui/icons-material';
import { alpha, InputBase } from '@mui/material';
import React from 'react';
import { AppContext, AppContextType } from '../../pages/_app';

export const SearchBar = () => {
    const { setSearchTerm } = React.useContext(
        AppContext
    ) as AppContextType;

    return <Search className='search' >
        <SearchIconWrapper className='search-icon-wra'>
            <SearchSharp className='search-icon' />
        </SearchIconWrapper>
        <StyledInputBase
            placeholder="Search…"
            inputProps={{ 'aria-label': 'search' }}
            onInput={(e) => setSearchTerm(e.target.value)}
        />
    </Search>
}



const Search = styled('div')(({ theme }) => ({
    position: 'relative',
    borderRadius: theme.shape.borderRadius,
    backgroundColor: alpha(theme.palette.common.white, 0.15),
    '&:hover': {
        backgroundColor: alpha(theme.palette.common.white, 0.25),
    },
    marginLeft: '13%',
    marginTop: '1%',
    width: '54% !important',
    [theme.breakpoints.up('sm')]: {
        marginLeft: theme.spacing(1),
        width: 'auto',
    },
}));

const SearchIconWrapper = styled('div')(({ theme }) => ({
    padding: theme.spacing(0, 2),
    height: '100%',
    position: 'absolute',
    pointerEvents: 'none',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
}));

const StyledInputBase = styled(InputBase)(({ theme }) => ({
    color: 'inherit',
    '& .MuiInputBase-input': {
        padding: theme.spacing(1, 1, 1, 0),
        // vertical padding + font size from searchIcon
        paddingLeft: `calc(1em + ${theme.spacing(4)})`,
        transition: theme.transitions.create('width'),
        width: '100%',
        [theme.breakpoints.up('sm')]: {
            width: '12ch',
            '&:focus': {
                width: '20ch',
            },
        },
    },
}));



