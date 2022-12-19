import streamlit as st
from conn_fun import run_query, download, query_to_df

# Global Variable function to get sc_no, supply_type, meter_address, grid_id
def get_query_based(query_based):
    rows = query_to_df(
            """
                select 
                    sc_no,
                    supply_type,
                    meter_address,
                    grid_id
                from 
                    microgrid_surveyhouseholdinfo 
                where 
                    meter_address ='{0}' 
                or 
                    meter_serial='{1}' 
                or 
                    sc_no ='{2}'
                limit
                    1;
            """.format(query_based, query_based, query_based))
    return rows

# Get Meter Data
def get_meter_data(query_based):
    rows = run_query(
        """
            select 
                sc_no, 
                site_id,
                grid_id, 
                meter_serial, 
                meter_address, 
                meter_mapping, 
                meter_sw_version,
                supply_type
            from 
                microgrid_surveyhouseholdinfo 
        where 
                meter_address ='{0}' 
            or 
                meter_serial='{1}' 
            or 
                sc_no ='{2}'
            limit
                1;
        """.format(query_based, query_based, query_based)
    )
    if len(rows) >= 1:
        st.write(rows)
        file_name = 'meter_data'
        download(rows, file_name)
    else:
        st.warning('Invalid Input')

# Get Tariff Data
def get_tariff(sc_no):
    rows = run_query(
        """
            select 
                *
            from 
                db_office.tbl_site_initialization 
            where 
                sc_no ='{0}' 
            order by 
                id
            desc 
                limit 1;
        """.format(sc_no)
    )
    if len(rows) >= 1:
        st.write(rows)
        file_name = 'tariff_data'
        download(rows, file_name)
    else:
        st.warning('No Data Available')

# Get All Tariff Data
def get_all_tariff_data(sc_no):
    rows = query_to_df(
        """
            select 
                *
            from 
                db_office.tbl_site_initialization 
            where 
                sc_no ='{0}';
        """.format(sc_no))
    if len(rows) >= 1:
        st.write(rows)
        file_name = 'all_tariff_data'
        download(rows, file_name)
    else:
        st.warning('No Data Available')

# Get User Info
def user_info(sc_no):
    rows = query_to_df(
        """
            select 
                *
            from 
                db_ems.user_meter_detail 
            where 
                serial_no ='{0}'
            and 
                user != '7976193118';
        """.format(sc_no))
    if len(rows) >= 1:
        st.write(rows)
        file_name = 'user_data'
        download(rows, file_name)
    else:
        st.warning('No Data Available')

# Limit Range for Single Phase
def supply_1ph(meter_address, grid_id, limit):
    rows = query_to_df(
        """
            select 
                *
            from 
                microgrid_nodehousehold_{1}
            where 
                meter_address ='{0}'
            order by 
                id
            desc
                limit {2};
        """.format(meter_address, grid_id, limit))
    if len(rows) >= 1:
        st.write(rows)
        file_name = 'custom_limit_data'
        download(rows, file_name)
    else:
        st.warning('No Data Available')

# Limit Range for Three Phase
def supply_3ph(meter_address, grid_id, limit):
    rows = query_to_df(
        """
            select 
                *
            from 
                microgrid_ecophousehold_{1}
            where 
                meter_address ='{0}'
            order by 
                id
            desc
                limit {2};
        """.format(meter_address, grid_id, limit))
    if len(rows) >= 1:
        st.write(rows)
        file_name = 'custom_limit_data'
        download(rows, file_name)
    else:
        st.warning('No Data Available')

# Get Data from Date Range for Single Phase Meter
def date_range_1ph(grid_id, meter_address, start_date, end_date):
    rows = query_to_df(
        """
            select 
                * 
            from 
                db_office.microgrid_nodehousehold_{0} 
            where 
                meter_address='{1}' 
            and 
                time >= '{2}' 
            and 
                time <= '{3}' 
            order by 
                id 
            desc;
        """.format(grid_id, meter_address, start_date, end_date))
    if len(rows) >= 1:
        st.write(rows)
        file_name = 'custom_date_range_data'
        download(rows, file_name)
    else:
        st.warning('No Data Available')

# Get Data from Date Range for Single Phase Meter
def date_range_3ph(grid_id, meter_address, start_date, end_date):
    rows = query_to_df(
        """
            select 
                * 
            from 
                db_office.microgrid_ecophousehold_{0} 
            where 
                meter_address='{1}' 
            and 
                time >= '{2}' 
            and 
                time <= '{3}' 
            order by 
                id 
            desc;
        """.format(grid_id, meter_address, start_date, end_date)
    )
    if len(rows) >= 1:
        st.write(rows)
        file_name = 'custom_date_range_data'
        download(rows, file_name)
    else:
        st.warning('No Data Available')

# get_last_recharge_data
def get_last_recharge_data(sc_no):
    rows = run_query(
        '''
            select 
                * 
            from 
                db_ems.dom_recharge_history
            where 
                serial_no='{0}'
            and 
                user != '7976193118' 
            order by 
                id 
            desc 
                limit 1;
        '''.format(sc_no)
    )
    if len(rows) >= 1:
        st.write(rows)
        file_name = 'last_recharge'
        download(rows, file_name)
    else:
        st.warning('No Data Available')

# get_total_recharge_data

def get_total_recharge_data(sc_no):
    rows = run_query(
        '''
            select 
                * 
            from 
                db_ems.dom_recharge_history
            where 
                serial_no='{0}'
            and 
                user != '7976193118' 
            order by 
                id 
            desc;
        '''.format(sc_no)
    )
    if len(rows) >= 1:
        st.write(rows)
        file_name = 'total_recharge'
        download(rows, file_name)
    else:
        st.warning('No Data Available')


# Get Prepaid Status
def get_prepaid_status(meter_address):
    rows = run_query(
        '''
            select 
                * 
            from 
                db_office.vdcu_commandinfo 
            ci, 
                db_office.vdcu_commandexecution ce 
            where 
                ci.cmd_set_id = ce.cmd_set_id 
            and 
                ci.meter_ip='{0}' 
            and 
                ci.cmd_id= 326 
            order by 
                ci.cmd_set_id 
            desc;
        '''.format(meter_address)
    )
    if len(rows) >= 1:
        st.write(rows)
        file_name = 'prepaid_status'
        download(rows, file_name)
    else:
        st.warning('No Data Available')

# Get EB DG status Tariff Data
def get_eb_dg_status_tariff_data(meter_address):
    rows = run_query(
        '''
            select 
                is_supply_generator, 
                current_running_price, 
                EG_energy_consumed, 
                EG_energy_amount_deducted, 
                DG_energy_consumed, 
                DG_energy_amount_deducted 
            from 
                db_office.microgrid_ecophousehold_31861
            where 
                meter_address='{0}' 
            order by 
                id 
            desc 
                limit 1;
        '''.format(meter_address)
    )
    if len(rows) >= 1:
        st.write(rows)
        file_name = 'eb_dg_status_tariff_data'
        download(rows, file_name)
    else:
        st.warning('No Data Available')

# Get Relay Status Load Data
def get_relay_status_load_data(supply_type, meter_address, grid_id):
    # Single Phase
    if supply_type == '1-Ph':    
        rows = run_query(
            '''
                select 
                    active_power 
                from 
                    db_office.microgrid_nodehousehold_{1}
                where 
                    meter_address='{0}' 
                order by 
                    id 
                desc 
                    limit 1;
            '''.format(meter_address, grid_id)
        )
        # return rows
        if rows['active_power'][0] == 0:
            relay_off = '<p style="font-family:sans-serif; color:red; font-size: 15px;">Relay Off</p>'
            return st.markdown(relay_off, unsafe_allow_html=True)
        else:
            relay_on = '<p style="font-family:sans-serif; color:Green; font-size: 15px;">Relay On</p>'
            st.write(rows)
            return st.markdown(relay_on, unsafe_allow_html=True)

    # Three Phase
    elif supply_type == '3-Ph':
        rows = run_query(
            '''
                select 
                    inst_load 
                from 
                    db_office.microgrid_ecophousehold_{1} 
                where 
                    meter_address='{0}' 
                order by 
                    id 
                desc 
                    limit 1;
            '''.format(meter_address, grid_id)
        )
        if rows['inst_load'][0] == 0:
            relay_off = '<p style="font-family:sans-serif; color:red; font-size: 15px;">Relay Off</p>'
            return st.markdown(relay_off, unsafe_allow_html=True)
        else:
            relay_on = '<p style="font-family:sans-serif; color:Green; font-size: 15px;">Relay On</p>'
            st.write(rows)
            return st.markdown(relay_on, unsafe_allow_html=True)

# Get Other Commands Data
def get_commands_data(meter_address):
    rows = run_query(
        '''
            select 
                * 
            from 
                db_office.vdcu_commandinfo 
            ci, 
                db_office.vdcu_commandexecution ce 
            where 
                ci.cmd_set_id = ce.cmd_set_id 
            and 
                ci.meter_ip='{0}' 
            order by 
                ci.cmd_set_id 
            desc;

        '''.format(meter_address)
    )
    if len(rows) >= 1:
        st.write(rows)
        file_name = 'commands_data'
        download(rows, file_name)
    else:
        st.warning('No Data Available')

# Replace Meter Data
def get_replace_meter_data(sc_no):
    rows = run_query(
        '''
            select 
                * 
            from 
                db_office.microgrid_meter_replace 
            where 
                sc_no='{0}'
            order by 
                id 
            desc;
        '''.format(sc_no)
    )
    if len(rows) >= 1:
        st.write(rows)
        file_name = 'replaced_meter_data'
        download(rows, file_name)
    else:
        st.warning('No Data Available')

# Churned Meter Data
def get_churned_meter_data(sc_no):
    rows = run_query(
        '''
            select 
                * 
            from 
                db_office.churned_meters_data 
            where 
                sc_no ='{0}';
        '''.format(sc_no)
    )
    if len(rows) >= 1:
        st.write(rows)
        file_name = 'churned_meter_data'
        download(rows, file_name)
    else:
        st.warning('No Data Available')