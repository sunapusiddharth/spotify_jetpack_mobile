import * as React from "react";
import NoSsr from "@mui/material/NoSsr";
import { Box, Button, CircularProgress, List, ListItem, Typography } from "@mui/material";
import { findSongsByFileNames, uploadMultipleSongs } from "../api";
import { AppContext, AppContextType } from "./_app";
import { SongType } from "../types/Song.type";
import Dropzone from "react-dropzone";
import UplaodSongAccordian from "../components/UplaodSongsAccordian";

interface PageProps {
}

const UploadComp: React.FC<PageProps> = ({
}: PageProps) => {
  const [songsSuggestions, setSongsSuggestions] = React.useState<{
    query: string;
    results: SongType[] | {
      id: string;
      name: string;
      playlist_id: any;
      playlist_name: string;
      artists: any[];
      duration: number;
      likes: number;
      genres: any[];
      album: any;
      thumbnail: string;
      view_count: number;
    }[];
  }[]>([])
  const [uploadedsongs, setUploadedSongs] = React.useState<{ song: any, file: File }[]>([])
  const [suceesUpload, setSuceesUpload] = React.useState<{ status: 1 | 2, message: string }[]>([])
  const [uploading, setUploading] = React.useState<boolean>(false)
  const [selectedSong, setselectedSong] = React.useState<{ forRow: number, selectedIndex: number, song: any, searchTerm: string }[]>([])
  // React.useEffect(() => {
  //   if (!session) {
  //     router.push("/auth/signin");
  //   }
  // }, []);

  const { user } = React.useContext(
    AppContext
  ) as AppContextType;
  const onFilesDrop = async (acceptedFiles: File[]) => {
    if (!user) return;
    // '5 Seconds of Summer - No Shame'.replace(/\.[^/.]+$/, "").replace(/[^a-zA-Z0-9]/g, ' ')
    // x.name.replace(/\.[^/.]+$/, "").replace(/[^a-zA-Z]/g,' ').trim()
    // x.name.replace(/\.[^/.]+$/, "").replace(/[^a-zA-Z0-9]/g, ' ').trim()
    const fileNames = acceptedFiles.map(x => x.name.replace(/\.[^/.]+$/, "").replace(/[^a-zA-Z]/g, ' ').trim())
    const songIds = acceptedFiles.map(x => x.name)
    console.log("filenames", songIds, fileNames)
    const songs = await findSongsByFileNames(user?.id, fileNames);
    // const songs = await findSongsByIds(user?.id, songIds);
    setSongsSuggestions(songs)
    setUploadedSongs(acceptedFiles.map((x, i) => ({ song: songs[i], file: acceptedFiles[i] })))

  }

  const uploadAllSongs = async () => {
    console.log("uploadAllSongs before", uploadAllSongs)
    uploadedsongs.forEach((uploadedsong, index) => {
      const x = selectedSong.find(r => r.forRow == index)
      uploadedsong.song.id = x?.song ? x.song.id : uploadedsong.song.results?.length ? uploadedsong.song.results[0].id : uploadedsong.song.id;
    })
    console.log("uploadAllSongs after", uploadedsongs)
    setUploading(true)
    const keys = await uploadMultipleSongs(uploadedsongs)
    setSuceesUpload(keys)
    setUploading(false)
  }

  const selectsuggestionFofFile = (song: SongType | {
    id: string;
    name: string;
    playlist_id: any;
    playlist_name: string;
    artists: any[];
    duration: number;
    likes: number;
    genres: any[];
    album: any;
    thumbnail: string;
    view_count: number;
  }, index: number, suggIndex: number, searchTerm: string) => {
    // uploadedsongs[index].song.results = song;
    // setUploadedSongs([...uploadedsongs])
    const idx = selectedSong.findIndex(r => r.forRow == index)
    if (idx > -1) {
      selectedSong[idx].selectedIndex = suggIndex
      selectedSong[idx].song = song
      selectedSong[idx].searchTerm = searchTerm
    } else {
      setselectedSong([...selectedSong, { forRow: index, selectedIndex: suggIndex, song, searchTerm }])
    }
  }

  const clearAll = () => {
    setUploadedSongs([])
    setSongsSuggestions([])
    setSuceesUpload([])
  }
  return (
    <NoSsr>
      <Box sx={{
        overflowY: 'scroll',

      }}>
        {/* mid side */}
        <Typography variant="h3">Upload</Typography>
        <Typography variant="subtitle1">
          Help us in populating content.
        </Typography>
        <Typography variant="subtitle2">
          Select the song corresponding to mp3 and finally click on upload to add all files.
        </Typography>
        <Dropzone onDrop={acceptedFiles => onFilesDrop(acceptedFiles)}>
          {({ getRootProps, getInputProps }) => (
            <section>
              <div {...getRootProps()}>
                <input {...getInputProps()} />
                <p>Drag n drop some files here, or click to <span className="select-files">select</span> files</p>
              </div>
            </section>
          )}
        </Dropzone>
        <List sx={{ marginTop: 5 }} >
          {songsSuggestions?.map((song, index) => <ListItem key={'playlist-song-' + index} className={'playlist_song'}>
            <UplaodSongAccordian index={index} suggestions={song} selectsuggestionFofFile={selectsuggestionFofFile} uploadedsong={uploadedsongs[index].song.name}
              uploadedFile={uploadedsongs[index]} selectedSong={selectedSong} />
          </ListItem>)}
        </List>
        <div style={{ display: 'flex', columnGap: '3%' }}>
          <Button variant="contained" onClick={uploadAllSongs} disabled={uploading}>{uploading ? <CircularProgress size={12} /> : 'Upload All'}</Button>
          <Button variant="contained" onClick={clearAll} disabled={uploading}>Clear</Button>
        </div>
        <div>{suceesUpload?.map((x, i) => <p>Uploaded song {i} {x.status == 1 ? 'Success' : 'Failed'} with details {JSON.stringify(x)}</p>)}</div>
      </Box>
    </NoSsr>
  );
};

export const UploadPage = React.memo(
  UploadComp
);
export default UploadPage;
