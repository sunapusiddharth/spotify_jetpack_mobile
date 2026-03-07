import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import { Card } from '@mui/material'
import { AppContext, AppContextType } from "../../pages/_app";
import { RadioStation } from "../../types/RadioStationType";
import Image from 'next/image'
import { memo, useContext } from 'react';
interface CompProps {
    station: RadioStation
}
function CardComp({ station }: CompProps) {
    const { setStation } = useContext(
        AppContext
    ) as AppContextType;
    return <><div className='img_container'><Card
        className='radio_carousel_card'
        sx={{
            width: 200, height: 230, 
            display: 'flex',
            flexDirection: 'column',
            alignContent: 'center',
            alignItems: 'center',
            justifyContent: ' space-around',
            background: 'unset',
            '&:hover': {
                cursor: 'pointer'
            },
        }} onClick={() => setStation(station)}>
        <Image src={station.favicon || "/sample.jpg"} 
        width={200} height={200} 
        alt="" />
        <img src="/play_icon.png" className='play' />
        <CardContent sx={{ fontSize: 'small' }}>
            <Typography variant="caption" textAlign={'center'}>{station.name}</Typography>
        </CardContent>
    </Card >
    </div >
    </>
}

function moviePropsAreEqual(
    prevProps: CompProps,
    nextProps: CompProps
) {
    return prevProps === nextProps;
}

export const RadioStationCard = memo(
    CardComp,
    moviePropsAreEqual
);
