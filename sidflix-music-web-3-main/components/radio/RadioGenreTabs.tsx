import Tabs from '@mui/material/Tabs';
import Box from '@mui/material/Box';
import { AppContext, AppContextType } from '../../pages/_app';
import { Tab, Typography } from '@mui/material';
import { RadioListing } from './RadioListing';
import { useContext, useState } from 'react';

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
export default function RadioGenreTabs({ countryCode, genres }: {
    countryCode: string, genres: {
        value: string;
        count: number;
    }[] | undefined
}) {
    const { user } = useContext(
        AppContext
    ) as AppContextType;
    const [value, setValue] = useState(0);
    const handleChange = (event: React.SyntheticEvent, newValue: number) => {
        setValue(newValue);
    };

    return (
        <Box sx={{ width: '100%' }}>
            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                <Tabs value={value} onChange={handleChange} aria-label="search tabs" variant="scrollable" scrollButtons="auto" textColor="secondary"
                    indicatorColor="secondary">
                    {genres?.map((genre, index) => <Tab label={<Box sx={{ display: 'flex',alignItems:'baseline',columnGap:1 }}><Typography sx={{ fontSize: 'small' }}>
                        {genre.value ? genre.value.toUpperCase() : 'All'}
                    </Typography><Typography sx={{ fontSize: 'x-small' }}>({genre.count})</Typography></Box>} {...a11yProps(index)} key={'genre-tab-labels' + index} />)}
                </Tabs>
            </Box>
            {genres?.map((genre, index) => <TabPanel value={value} index={index} key={genre.value + index + value}>
                <RadioListing countryCode={countryCode} genre={genre.value} key={'genre-tab-panels' + index} />
            </TabPanel>)}
        </Box>
    );
}
