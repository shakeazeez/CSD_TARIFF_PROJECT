import notfound from '../assets/404.png'

export function NotFound(){
    return (
    <>
    <div style={{ backgroundColor: 'grey', width: 750, height: 550, alignContent: "center" }}>
        <img src={notfound} width={250} height={250} />
    </div>
    </>
    );
}