import Dropdown from "./Dropdown";

// WIP

export function SearchCard(){

    const countryOptions = [
        { code: "702", id: "Singapore" },
        { code: "840", id: "United States" }
    ];
    
    //background currently set to blue to represent the card
    return(
        <>
        <div style={{background:"blue", justifyContent:"center"}}>
            <div style={{ display:"flex", justifyContent:"center"}}>
                <Dropdown title="To" options={countryOptions} onChange={(selected) => console.log(selected)}/>
                <Dropdown title="From" options={countryOptions} onChange={(selected) => console.log(selected)}/>
            </div>
            <div style={{ display:"flex", justifyContent:"center"}}>
                <p>Item:</p>
                <input
                    type="text"
                    id="hs"
                    onChange={e => setHS((e.target.value).toLowerCase())}
                />
                <p>Cost:</p>
                <input
                    type="number"
                    id="cost"
                    onChange={e => setCost(e.target.value)}
                /><br/>
            </div>
            <button>Search</button>
        </div>
        </>
    );
}