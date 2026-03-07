import * as React from 'react';
import Box from '@mui/material/Box';
import LinearProgress from '@mui/material/LinearProgress';
import { Slider, Typography } from '@mui/material';
import { secondsToMinutesAndSeconds } from '../utils';
import { useTheme } from '@emotion/react';
import ReactPlayer from 'react-player';

export default function ProgressBar({ progress, setProgress, buffer, playNext, setBuffer, duration, playerRef }: {
    progress: number, setProgress: (s: number) => void, buffer: number,
    playNext: any
    setBuffer: (s: number) => void,
    duration: number,
    playerRef: React.RefObject<ReactPlayer>
}) {

    const theme = useTheme()
    const progressRef = React.useRef(() => { });
    React.useEffect(() => {
        progressRef.current = () => {
            if (progress > 100) {
                playNext()
                setProgress(0);
                setBuffer(0);
            }
        };
    });

    React.useEffect(() => {
        const timer = setInterval(() => {
            progressRef.current();
        }, 500);

        return () => {
            clearInterval(timer);
        };
    }, []);
    console.log("progress", progress, buffer, duration)
    return (
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <Box sx={{ width: '100%' }}>
                <LinearProgress variant="buffer" value={progress} valueBuffer={buffer} />
            </Box>
            <Box sx={{ minWidth: 35 }}>
                <Typography variant="body2" color="text.secondary">{secondsToMinutesAndSeconds(duration)}</Typography>
            </Box>

            <Slider
                aria-label="time-indicator"
                size="small"
                value={progress}
                min={0}
                step={1}
                max={duration}
                onChange={(_, value) => {
                    console.log("onChange seeking to new value:",value,)
                    playerRef.current?.seekTo(value as number)
                }}
                sx={{
                    color: '#fff',
                    height: 4,
                    '& .MuiSlider-thumb': {
                        width: 8,
                        height: 8,
                        transition: '0.3s cubic-bezier(.47,1.64,.41,.8)',
                        '&:before': {
                            boxShadow: '0 2px 12px 0 rgba(0,0,0,0.4)',
                        },
                        '&:hover, &.Mui-focusVisible': {
                            boxShadow: `0px 0px 0px 8px rgb(255 255 255 / 16%)`,
                        },
                        '&.Mui-active': {
                            width: 20,
                            height: 20,
                        },
                    },
                    '& .MuiSlider-rail': {
                        opacity: 0.28,
                    },
                }}
            />
        </Box>

    );
}
