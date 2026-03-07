import * as React from 'react';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Box from '@mui/material/Box';
import ArtistOverview from './ArtistOverview';
import SimilarArtists from './SimilarArtists';
import ArtistPlayLists from './ArtistPlayLists';
import { ArtistType } from '../../types/ArtistType';
import ArtistAlbums from './ArtistAlbums';

interface TabPanelProps {
    children?: React.ReactNode;
    index: number;
    value: number;
}

function TabPanel(props: TabPanelProps) {
    const { children, value, index, ...other } = props;

    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`simple-tabpanel-${index}`}
            aria-labelledby={`simple-tab-${index}`}
            {...other}
        >
            {value === index && <>{children}</>}
        </div>
    );
}

function a11yProps(index: number) {
    return {
        id: `simple-tab-${index}`,
        'aria-controls': `simple-tabpanel-${index}`,
    };
}

export default function ArtistTabs({ artist }: { artist: ArtistType }) {
    const [value, setValue] = React.useState(0);

    const handleChange = (event: React.SyntheticEvent, newValue: number) => {
        setValue(newValue);
    };

    return (
        <Box sx={{ width: '100%', mt: 15, ml: 2 }}>
            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                <Tabs value={value} onChange={handleChange} aria-label="search tabs">
                    <Tab label="Overview" {...a11yProps(0)} />
                    <Tab label="Albums" {...a11yProps(1)} />
                    <Tab label="Similar Artists" {...a11yProps(2)} />
                    <Tab label="Playlists" {...a11yProps(3)} />
                </Tabs>
            </Box>
            <TabPanel value={value} index={0}>
                <ArtistOverview artist={artist} />
            </TabPanel>
            <TabPanel value={value} index={1}>
                <ArtistAlbums artistId={artist.id} />
            </TabPanel>
            <TabPanel value={value} index={2}>
                <SimilarArtists similar={artist.similar} />
            </TabPanel>
            <TabPanel value={value} index={3}>
                <ArtistPlayLists artistId={artist.id} />
            </TabPanel>
        </Box>
    );
}
