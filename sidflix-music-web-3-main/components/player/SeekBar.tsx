import styled from "@emotion/styled";
import { Typography, Slider, Box, LinearProgress } from "@mui/material";
import ReactPlayer from "react-player";

const TinyText = styled(Typography)({
    color: 'white',
    fontSize: '0.75rem',
    // opacity: 0.38,
    fontWeight: 500,
    // letterSpacing: 0.2,
});

function formatDuration(value: number) {
    value = parseInt(value)
    const minute = Math.floor(value / 60);
    const secondLeft = value - minute * 60;
    return `${minute}:${secondLeft < 10 ? `0${secondLeft}` : secondLeft}`;
}
export const SeekBar = (
    {
        progress, duration,
        playerRef, buffer,
        playBackError
    }: {
        progress: number,
        duration: number,
        playerRef: React.RefObject<ReactPlayer>,
        buffer: number,
        playBackError: string|undefined
    }
) => {
    return <>
        {playBackError && <Typography variant="caption" color={'red'}>Something went wrong, please play other song.{playBackError}</Typography>}

        <Slider
            aria-label="time-indicator"
            size="small"
            value={progress}
            min={0}
            step={1}
            max={duration}
            onChange={(_, value) => {
                playerRef.current?.seekTo(value as number)
            }}
            sx={{
                color: 'white',
                height: 6,
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
        >
            <LinearProgress variant="buffer" value={progress} valueBuffer={buffer} />
        </Slider>
        <Box
            sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                ml: 2,
            }}
        >
            <TinyText>{formatDuration(progress)}</TinyText>
            <TinyText>/{formatDuration(duration - progress)}</TinyText>
        </Box></>
}