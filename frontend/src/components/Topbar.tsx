import { Menu, X} from 'lucide-react';
import MenuButton from "./buttons/MenuButton.tsx";
import clsx from "clsx";

type props = {
    sidebarToggled: boolean,
    setSidebarToggled: Function
    className?: string;
}

const Topbar = ({sidebarToggled, setSidebarToggled, className}: props) => {
    return (
        <div className={clsx(className)}>
            <MenuButton onClick={() => setSidebarToggled(!sidebarToggled)}>
                {sidebarToggled ? <Menu size={24}/> :  <X size={24}/>}
            </MenuButton>
        </div>
    )
}

export default Topbar;