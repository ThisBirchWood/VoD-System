import { Menu, X} from 'lucide-react';
import MenuButton from "./buttons/MenuButton.tsx";

type props = {
    sidebarToggled: boolean,
    setSidebarToggled: Function
}

const Topbar = ({sidebarToggled, setSidebarToggled}: props) => {
    return (
        <div>
            <MenuButton onClick={() => setSidebarToggled(!sidebarToggled)}>
                {sidebarToggled ? <X size={24}/> : <Menu size={24}/>}
            </MenuButton>
        </div>
    )
}

export default Topbar;