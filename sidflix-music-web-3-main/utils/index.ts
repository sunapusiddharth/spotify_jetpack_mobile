import { UserType } from "../types/User.type";

export function numberWithCommas(x: number) {
  return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

export function splitOnCaps(x: string) {
  const wordRegex = /[A-Z]?[a-z]+|[0-9]+|[A-Z]+(?![a-z])/g;
  const result = x.match(wordRegex)?.join(' ');
  return result
}

export function capitalize(x:string){
  return x.charAt(0).toUpperCase()+x.slice(1)
}

export function secondsToHm(d: number) {
  d = Number(d);
  var h = Math.floor(d / 3600);
  var m = Math.floor(d % 3600 / 60);
  var s = Math.floor(d % 3600 % 60);

  var hDisplay = h > 0 ? h + (h == 1 ? " hour, " : " hours, ") : "";
  var mDisplay = m > 0 ? m + (m == 1 ? " min " : " min ") : "";
  // var sDisplay = s > 0 ? s + (s == 1 ? " second" : " seconds") : "";
  return hDisplay + mDisplay;
}


export function secondsToMinutesAndSeconds(d: number) {
  if (!d) return ''
  d = Number(d);
  var m = Math.floor(d % 3600 / 60);
  var s = Math.floor(d % 3600 % 60);

  var mDisplay = m > 0 ? m + (m == 1 ? " minute, " : " minutes, ") : "";
  var sDisplay = s > 0 ? s + (s == 1 ? " second" : " seconds") : "";
  return m.toString().padStart(2, '0') + ':' + s.toString().padStart(2, '0');
}

export function millisToMinutesAndSeconds(millis: number) {
  var minutes = Math.floor(millis / 60000);
  var seconds = ((millis % 60000) / 1000).toFixed(0) as unknown as number;
  return minutes + ":" + (seconds < 10 ? '0' : '') + seconds;
}


export function checkIfUserHasLikedSong(user: UserType | undefined, songid: string) {
  return Boolean(!user?.liked_songs?.includes(songid))
}

export function checkIfUserHasLikedPodcast(user: UserType | undefined, songid: string) {
  return Boolean(!user?.liked_podcast?.includes(songid))
}
export function checkIfUserHasLikedRadio(user: UserType | undefined, songid: string) {
  return Boolean(!user?.liked_radio?.includes(songid))
}


export function getchunks<T>(arr: T[], len: number): T[][] {

  var chunks: T[][] = [],
    i = 0,
    n = arr.length;

  while (i < n) {
    const slicedata: T[] = arr.slice(i, i += len)
    chunks.push(slicedata);
  }

  return chunks;
}

export function colorChanger() {
  const bgs = [
    {
      backgroundColor: '#0093E9',
      backgroundImage: 'linear-gradient(160deg, #0093E9 0%, #80D0C7 100%)'
    },
    {
      backgroundColor: '#4158D0',
      backgroundImage: 'linear-gradient(43deg, #4158D0 0%, #C850C0 46%, #FFCC70 100%)'
    },
    {
      backgroundColor: '#FA8BFF',
      backgroundImage: 'linear-gradient(45deg, #FA8BFF 0%, #2BD2FF 52%, #2BFF88 90%)'
    },
    {
      backgroundColor: '#FF9A8B',
      backgroundImage: 'linear-gradient(90deg, #FF9A8B 0%, #FF6A88 55%, #FF99AC 100%)'
    },
    {
      // background: '#834d9b',  /* fallback for old browsers */
      background: '-webkit-linear-gradient(to right, #d04ed6, #834d9b)',  /* Chrome 10-25, Safari 5.1-6 */
      // background: 'linear-gradient(to right, #d04ed6, #834d9b)' /* W3C, IE 10+/ Edge, Firefox 16+, Chrome 26+, Opera 12+, Safari 7+ */

    }
  ]
  const idx = randomIntFromInterval(0, bgs.length)
  // console.log("color",bgs[idx])
  return bgs[idx]
}

function randomIntFromInterval(min: number, max: number) { // min and max included 
  return Math.floor(Math.random() * (max - min + 1) + min)
}


  export const uniqueBy = <T>(uniqueKey: keyof T, objects: T[]): T[] => {
    const ids = objects.map(object => object[uniqueKey]);
    return objects.filter((object, index) => !ids.includes(object[uniqueKey], index + 1));
  } 


  

export  function removeTags(str:string) {
    if ((str===null) || (str===''))
        return false;
    else
        str = str?.toString();
          
    // Regular expression to identify HTML tags in
    // the input string. Replacing the identified
    // HTML tag with a null string.
    return str?.replace( /(<([^>]+)>)/ig, '');
}



export function getImageUrl(url:string){
if(!url.includes('http') || !url.includes('https')){
  return "/sample.jpg"
}
return url
}